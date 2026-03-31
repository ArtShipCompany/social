import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';

// Вспомогательная функция для запросов (использует authApi.fetchWithErrorHandling)
async function request(url, options = {}) {
  return fetchWithErrorHandling(url, {
    credentials: 'include',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
}

// Для FormData-запросов (без Content-Type, чтобы браузер сам выставил boundary)
async function requestMultipart(url, options = {}) {
  return fetchWithErrorHandling(url, {
    credentials: 'include',
    ...options,
    // 👇 НЕ добавляем Content-Type — браузер сделает это сам
    headers: {
      ...options.headers,
    },
  });
}

export const userApi = {
  // PUBLIC
  
  async getPublicUser(id) {
    const response = await fetch(`${API_URL}/users/public/${id}`, { credentials: 'include' });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Пользователь не найден');
      throw new Error(`HTTP ${response.status}`);
    }
    return await response.json();
  },

  // PRIVATE

  async getCurrentUser() {
    const userData = await authApi.fetchProtected(`${API_URL}/users/me`);
    return this.formatUser(userData);
  },

  async getUserById(id) {
    const userData = await authApi.fetchProtected(`${API_URL}/users/${id}`);
    return userData;
  },

  async updateProfile(userData) {
    const formData = new FormData();
    
    if (userData.username) formData.append('username', userData.username);
    if (userData.displayName) formData.append('displayName', userData.displayName);
    if (userData.bio) formData.append('bio', userData.bio);
    if (userData.isPublic !== undefined) {
      formData.append('isPublic', userData.isPublic.toString());
    }
    
    const result = await requestMultipart(`${API_URL}/users/me`, {
      method: 'PUT',
      body: formData,
    });
    
    return this.formatUser(result.user || result);
  },

  async updateProfileWithAvatar(formData) {
    const result = await requestMultipart(`${API_URL}/users/me`, {
      method: 'PUT',
      body: formData,
    });
    return this.formatUser(result.user || result);
  },

  async uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    return requestMultipart(`${API_URL}/users/me/avatar`, {
      method: 'POST',
      body: formData,
    });
  },

  async deleteAvatar() {
    return authApi.fetchProtected(`${API_URL}/users/me/avatar`, {
      method: 'DELETE'
    });
  },
  
  async getAllUsers() {
    const usersData = await authApi.fetchProtected(`${API_URL}/users`);
    return Array.isArray(usersData) ? usersData.map(user => this.formatUser(user)) : [];
  },

  async getUserByUsername(username) {
    const userData = await authApi.fetchProtected(`${API_URL}/users/username/${username}`);
    return this.formatUser(userData);
  },
  
  async getUser(userId, isAuthenticated = false) {
    try {
      if (isAuthenticated) {
        try {
          const userData = await this.getUserById(userId);
          return this.formatUser(userData);
        } catch {
          const publicData = await this.getPublicUser(userId);
          return this.formatUser(publicData);
        }
      } else {
        return this.formatUser(await this.getPublicUser(userId));
      }
    } catch (error) {
      console.error('[User API] Error getting user:', error);
      throw error;
    }
  },
  
  createProfileFormData(data) {
    const formData = new FormData();
    if (data.avatarFile) formData.append('avatarFile', data.avatarFile);
    if (data.username) formData.append('username', data.username);
    if (data.displayName !== undefined) formData.append('displayName', data.displayName);
    if (data.bio !== undefined) formData.append('bio', data.bio);
    if (data.isPublic !== undefined) formData.append('isPublic', data.isPublic.toString());
    return formData;
  },
  
  getAvatarUrl(user) {
    if (!user) return '/default-avatar.png';
    const avatarUrl = user.avatarUrl || user.pfp || user.avatar;
    if (!avatarUrl) return '/default-avatar.png';
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) return avatarUrl;
    if (avatarUrl.startsWith('/uploads/images/') || avatarUrl.startsWith('/api/files/images/')) return avatarUrl;
    if (avatarUrl.includes('.')) return `/uploads/images/${avatarUrl}`;
    return '/default-avatar.png';
  },
  
  getFullUrl(path) {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://')) return path;
    let finalPath = path;
    if (path.startsWith('/api/files/images/')) {
      const filename = path.split('/').pop();
      finalPath = `/uploads/images/${filename}`;
    } else if (path.startsWith('/uploads/')) {
      finalPath = path;
    } else if (path.startsWith('uploads/')) {
      finalPath = `/${path}`;
    } else if (!path.includes('/')) {
      finalPath = `/uploads/images/${path}`;
    }
    return `http://localhost:8081${finalPath}`;
  },
  
  formatUser(user) {
    if (!user) return null;
    const formattedUser = {
      id: user.id,
      username: user.username,
      displayName: user.displayName || user.username,
      email: user.email,
      bio: user.bio || '',
      avatarUrl: this.getAvatarUrl(user),
      isPublic: user.isPublic !== false,
      createdAt: user.createdAt
    };
    if (formattedUser.avatarUrl && !formattedUser.avatarUrl.startsWith('http')) {
      formattedUser.avatarUrl = this.getFullUrl(formattedUser.avatarUrl);
    }
    return formattedUser;
  }
};

export default userApi;