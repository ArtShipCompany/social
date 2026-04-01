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
  return {
    id: follow.id,
    follower: formatUserPreview(follow.follower),
    following: formatUserPreview(follow.following),
    createdAt: follow.createdAt,
  };
};

const formatFollowPage = (pageData) => {
  if (!pageData?.content) return pageData;
  return {
    ...pageData,
    content: pageData.content.map(follow => formatFollow(follow)),
  };
};

const formatUserPreview = (user) => {
  if (!user) return null;
  return {
    id: user.id,
    username: user.username,
    displayName: user.displayName || user.username,
    avatarUrl: user.avatarUrl || user.pfp || '/default-avatar.png',
    isPublic: user.isPublic !== false,
  };
};


export const followApi = {
  
  // Подписаться на пользователя
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
    const data = await request(`${BASE_PATH}/me/following/search?${params}`);
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
    return pageData.content.map(follow => {
      const user = type === 'following' ? follow.following : follow.follower;
      return formatUserPreview(user);
    }).filter(Boolean);
  },
  
  utils: {
    formatFollow,
    formatFollowPage,
    formatUserPreview,
  }
};

export default followApi;