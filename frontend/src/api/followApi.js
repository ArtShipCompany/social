import { authApi } from './authApi';

const API_URL = 'http://localhost:8081/api';
const BASE_PATH = `${API_URL}/follow`;

const request = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

const formatFollow = (follow) => {
  if (!follow) return null;
  
  const follower = follow.follower ? {
    id: follow.follower.id,
    username: follow.follower.username,
    displayName: follow.follower.displayName || follow.follower.username,
    avatarUrl: follow.follower.avatarUrl || '/default-avatar.png',
    isPublic: true,
  } : null;
  
  const following = follow.following ? {
    id: follow.following.id,
    username: follow.following.username,
    displayName: follow.following.displayName || follow.following.username,
    avatarUrl: follow.following.avatarUrl || '/default-avatar.png',
    isPublic: true,
  } : null;
  
  return {
    id: follow.id || `${follower?.id}-${following?.id}`,
    followerId: follower?.id,
    followingId: following?.id,
    follower,
    following,
    createdAt: follow.createdAt,
  };
};

const formatFollowPage = (pageData) => {
  console.log('[followApi] formatFollowPage input:', pageData);
  
  if (Array.isArray(pageData)) {
    console.log('[followApi] Получен массив, оборачиваем');
    return {
      content: pageData.map(follow => formatFollow(follow)),
      totalElements: pageData.length,
      last: true
    };
  }
  
  if (pageData?.content) {
    console.log('[followApi] Есть content, форматируем');
    return {
      ...pageData,
      content: pageData.content.map(follow => formatFollow(follow)),
    };
  }
  
  console.log('[followApi] Неизвестный формат, возвращаем пустой');
  return { content: [], totalElements: 0, last: true };
};

const formatUserPreview = (user) => {
  console.log('[followApi] formatUserPreview input:', user);
  
  if (!user) {
    console.log('[followApi] formatUserPreview: user is null');
    return null;
  }
  
  const formatted = {
    id: user.id,
    username: user.username,
    displayName: user.displayName || user.username,
    avatarUrl: user.avatarUrl || user.pfp || '/default-avatar.png',
    isPublic: user.isPublic !== false,
  };
  
  console.log('[followApi] formatUserPreview result:', formatted);
  return formatted;
};


export const followApi = {
  
  async follow(followingId) {
    const data = await request(`${BASE_PATH}/${followingId}`, { method: 'POST' });
    return formatFollow(data);
  },
  
  async unfollow(followingId) {
    await request(`${BASE_PATH}/${followingId}`, { method: 'DELETE' });
    return true;
  },
  
  async isFollowing(followingId) {
    const data = await request(`${BASE_PATH}/check/${followingId}`);
    return data?.isFollowing === true;
  },
  
  
  async getMyFollowers(page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    const data = await request(`${BASE_PATH}/me/followers/search?${params}`);
    return formatFollowPage(data);
  },
  
  async getMyFollowing(page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    const url = `${BASE_PATH}/me/following/search?${params}`;
    console.log('[followApi] getMyFollowing URL:', url);
    
    const data = await request(url);
    console.log('[followApi] getMyFollowing response:', data);
    
    if (data?.content?.length > 0) {
      console.log('[followApi] Первый элемент content:', data.content[0]);
      console.log('[followApi] Есть ли followingId или followerId?', {
        hasFollowingId: 'followingId' in data.content[0],
        hasFollowerId: 'followerId' in data.content[0],
        hasFollowing: 'following' in data.content[0],
        hasFollower: 'follower' in data.content[0]
      });
    }
    
    return formatFollowPage(data);
  },
  
  async getFollowers(userId, page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    const data = await request(`${BASE_PATH}/followers/${userId}/search?${params}`);
    return formatFollowPage(data);
  },
  
  async getFollowing(userId, page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    const data = await request(`${BASE_PATH}/following/${userId}/search?${params}`);
    return formatFollowPage(data);
  },
   
  async getFollowCounts(userId) {
    const data = await request(`${BASE_PATH}/count/${userId}`);
    return {
      followers: data?.followers ?? 0,
      following: data?.following ?? 0,
    };
  },
  
  async toggleFollow(followingId) {
    const isFollowing = await this.isFollowing(followingId);
    if (isFollowing) {
      await this.unfollow(followingId);
      return { following: false, action: 'unfollowed' };
    } else {
      await this.follow(followingId);
      return { following: true, action: 'followed' };
    }
  },
  
  extractUsersFromPage(pageData, type = 'following') {
    if (!pageData?.content) return [];
    
    return pageData.content
      .map(follow => {
        const user = type === 'following' ? follow.following : follow.follower;
        return this.utils.formatUserPreview(user);
      })
      .filter(Boolean);
  },
  
  utils: {
    formatFollow,
    formatFollowPage,
    formatUserPreview,
  }
};

export default followApi;