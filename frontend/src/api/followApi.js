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

// Форматирует массив подписок с пагинацией
const formatFollowPage = (pageData) => {
  if (!pageData?.content) return pageData;
  return {
    ...pageData,
    content: pageData.content.map(follow => formatFollow(follow)),
  };
};

// Превью пользователя для списков подписок
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
    const data = await request(`${BASE_PATH}/${followingId}`, {
      method: 'POST',
    });
    return formatFollow(data);
  },
  
  // Отписаться от пользователя
  async unfollow(followingId) {
    await request(`${BASE_PATH}/${followingId}`, {
      method: 'DELETE',
    });
    return true;
  },
  
  // Проверить, подписан ли текущий пользователь на target
  async isFollowing(followingId) {
    const data = await request(`${BASE_PATH}/check/${followingId}`);
    return data?.isFollowing === true;
  },
  
  
  // Мои подписчики
  async getMyFollowers(page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    
    const data = await request(`${BASE_PATH}/me/followers/search?${params}`);
    return formatFollowPage(data);
  },
  
  // Мои подписки
  async getMyFollowing(page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    
    const data = await request(`${BASE_PATH}/me/following/search?${params}`);
    return formatFollowPage(data);
  },
  
  
  // Подписчики любого пользователя
  async getFollowers(userId, page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    
    const data = await request(`${BASE_PATH}/followers/${userId}/search?${params}`);
    return formatFollowPage(data);
  },
  
  // Подписки любого пользователя
  async getFollowing(userId, page = 0, size = 20, username = null) {
    const params = new URLSearchParams({ page, size: size.toString() });
    if (username) params.append('username', username);
    
    const data = await request(`${BASE_PATH}/following/${userId}/search?${params}`);
    return formatFollowPage(data);
  },
  
  
  // Количество подписчиков и подписок пользователя
  async getFollowCounts(userId) {
    return request(`${BASE_PATH}/count/${userId}`);
  },
  
  // Количество подписчиков текущего пользователя
  async getMyFollowerCount() {
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    if (!currentUser.id) throw new Error('Пользователь не авторизован');
    return this.getFollowCounts(currentUser.id);
  },
  
  
  // Тоггл подписки (удобно для кнопки "Подписаться/Отписаться")
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
  
  // Извлечь массив пользователей из страницы подписок
  extractUsersFromPage(pageData, type = 'following') {
    if (!pageData?.content) return [];
    return pageData.content.map(follow => {
      const user = type === 'following' ? follow.following : follow.follower;
      return formatUserPreview(user);
    }).filter(Boolean);
  },
  
  // Экспорт форматтеров для внешнего использования
  utils: {
    formatFollow,
    formatFollowPage,
    formatUserPreview,
  }
};

export default followApi;