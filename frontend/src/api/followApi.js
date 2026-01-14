// api/followApi.js
const API_URL = 'http://localhost:8081/api';

const getToken = () => {
  return localStorage.getItem('accessToken');
};

// Основная функция для запросов с обработкой ошибок
async function fetchWithErrorHandling(url, options = {}) {
  const finalOptions = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };
  
  // Добавляем токен если есть
  const token = getToken();
  if (token) {
    finalOptions.headers['Authorization'] = `Bearer ${token}`;
  }
  
  try {
    console.log(`[Follow API] ${options.method || 'GET'} ${url}`);
    const response = await fetch(url, finalOptions);
    
    if (response.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
      throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
    }
    
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      try {
        const data = await response.json();
        errorMessage = data.error || data.message || errorMessage;
      } catch {
        // Если не удалось распарсить JSON
      }
      throw new Error(errorMessage);
    }
    
    // Для DELETE запросов с 204 No Content
    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('[Follow API] Error:', error);
    throw error;
  }
}

export const followApi = {
  // Подписка на пользователя
  async followUser(followerId, followingId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/follower/${followerId}/following/${followingId}`,
        {
          method: 'POST'
        }
      );
    } catch (error) {
      console.error('[Follow API] Error following user:', error);
      throw error;
    }
  },
  
  // Отписка от пользователя
  async unfollowUser(followerId, followingId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/follower/${followerId}/following/${followingId}`,
        {
          method: 'DELETE'
        }
      );
    } catch (error) {
      console.error('[Follow API] Error unfollowing user:', error);
      throw error;
    }
  },
  
  // Проверка подписки
  async isFollowing(followerId, followingId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/follower/${followerId}/following/${followingId}/exists`
      );
    } catch (error) {
      console.error('[Follow API] Error checking follow status:', error);
      throw error;
    }
  },
  
  // Получить подписчиков пользователя
  async getFollowers(userId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/user/${userId}/followers`
      );
    } catch (error) {
      console.error('[Follow API] Error getting followers:', error);
      throw error;
    }
  },
  
  // Получить подписки пользователя
  async getFollowing(userId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/user/${userId}/following`
      );
    } catch (error) {
      console.error('[Follow API] Error getting following:', error);
      throw error;
    }
  },
  
  // Получить количество подписчиков
  async getFollowerCount(userId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/user/${userId}/followers/count`
      );
    } catch (error) {
      console.error('[Follow API] Error getting follower count:', error);
      throw error;
    }
  },
  
  // Получить количество подписок
  async getFollowingCount(userId) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/follows/user/${userId}/following/count`
      );
    } catch (error) {
      console.error('[Follow API] Error getting following count:', error);
      throw error;
    }
  },
  
  // === УПРОЩЕННЫЕ МЕТОДЫ ДЛЯ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ ===
  
  // Подписаться на пользователя (текущий пользователь -> targetUserId)
  async follow(targetUserId) {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        throw new Error('Пользователь не авторизован');
      }
      
      return await this.followUser(currentUser.id, targetUserId);
    } catch (error) {
      console.error('[Follow API] Error following:', error);
      throw error;
    }
  },
  
  // Отписаться от пользователя (текущий пользователь -> targetUserId)
  async unfollow(targetUserId) {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        throw new Error('Пользователь не авторизован');
      }
      
      return await this.unfollowUser(currentUser.id, targetUserId);
    } catch (error) {
      console.error('[Follow API] Error unfollowing:', error);
      throw error;
    }
  },
  
  // Проверить, подписан ли текущий пользователь на targetUserId
  async isCurrentUserFollowing(targetUserId) {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        return false;
      }
      
      return await this.isFollowing(currentUser.id, targetUserId);
    } catch (error) {
      console.error('[Follow API] Error checking if current user follows:', error);
      return false;
    }
  },
  
  // Получить подписчиков текущего пользователя
  async getCurrentUserFollowers() {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        throw new Error('Пользователь не авторизован');
      }
      
      return await this.getFollowers(currentUser.id);
    } catch (error) {
      console.error('[Follow API] Error getting current user followers:', error);
      throw error;
    }
  },
  
  // Получить подписки текущего пользователя
  async getCurrentUserFollowing() {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        throw new Error('Пользователь не авторизован');
      }
      
      return await this.getFollowing(currentUser.id);
    } catch (error) {
      console.error('[Follow API] Error getting current user following:', error);
      throw error;
    }
  },
  
  // Получить количество подписчиков текущего пользователя
  async getCurrentUserFollowerCount() {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        return 0;
      }
      
      return await this.getFollowerCount(currentUser.id);
    } catch (error) {
      console.error('[Follow API] Error getting current user follower count:', error);
      return 0;
    }
  },
  
  // Получить количество подписок текущего пользователя
  async getCurrentUserFollowingCount() {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.id) {
        return 0;
      }
      
      return await this.getFollowingCount(currentUser.id);
    } catch (error) {
      console.error('[Follow API] Error getting current user following count:', error);
      return 0;
    }
  },
  
  // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
  
  // Тоггл подписки (подписаться/отписаться)
  async toggleFollow(targetUserId) {
    try {
      const isFollowing = await this.isCurrentUserFollowing(targetUserId);
      
      if (isFollowing) {
        await this.unfollow(targetUserId);
        return { following: false, action: 'unfollowed' };
      } else {
        await this.follow(targetUserId);
        return { following: true, action: 'followed' };
      }
    } catch (error) {
      console.error('[Follow API] Error toggling follow:', error);
      throw error;
    }
  },
  
  // Форматировать данные подписчика/подписки
  formatFollowData(followData) {
    if (!followData) return null;
    
    return {
      id: followData.id,
      follower: followData.follower || {},
      following: followData.following || {},
      createdAt: followData.createdAt
    };
  },
  
  // Извлечь информацию о пользователях из массива followData
  extractUsersFromFollows(followsArray, type = 'following') {
    if (!Array.isArray(followsArray)) return [];
    
    return followsArray.map(follow => {
      const user = type === 'following' ? follow.following : follow.follower;
      return {
        id: user?.id,
        username: user?.username,
        displayName: user?.displayName || user?.username,
        avatarUrl: user?.avatarUrl || user?.pfp || '/default-avatar.png'
      };
    }).filter(user => user.id);
  }
};

export default followApi;