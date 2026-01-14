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
  if (token && !url.includes('/public/')) {
    finalOptions.headers['Authorization'] = `Bearer ${token}`;
  }
  
  try {
    const response = await fetch(url, finalOptions);
    
    if (response.status === 401) {
      // Токен истек или недействителен
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
      throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
    }
    
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      try {
        const data = await response.json();
        errorMessage = data.message || data.error || errorMessage;
      } catch {
        // Если не удалось распарсить JSON
      }
      throw new Error(errorMessage);
    }
    
    // Для DELETE запросов может не быть тела
    if (response.status === 204 || finalOptions.method === 'DELETE') {
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('[API] Fetch error:', error);
    throw error;
  }
}

export const userApi = {
  // === ОСНОВНЫЕ МЕТОДЫ ДЛЯ ПОЛУЧЕНИЯ ПОЛЬЗОВАТЕЛЯ ===
  
  // Универсальный метод для получения пользователя (автоматически выбирает между публичным и приватным доступом)
  async getUser(userId, isAuthenticated = false) {
    try {
      if (isAuthenticated) {
        // Пытаемся получить полные данные
        try {
          const userData = await this.getUserById(userId);
          console.log('[User API] Got full user data');
          return this.formatUser(userData);
        } catch (authError) {
          // Если не удалось получить полные данные, получаем публичные
          console.log('[User API] Falling back to public data:', authError.message);
          const publicData = await this.getPublicUser(userId);
          return this.formatUser(publicData);
        }
      } else {
        // Для неавторизованных - только публичные данные
        const publicData = await this.getPublicUser(userId);
        return this.formatUser(publicData);
      }
    } catch (error) {
      console.error('[User API] Error getting user:', error);
      throw error;
    }
  },
  
  // Получить публичного пользователя по ID
  async getPublicUser(id) {
    try {
      const response = await fetch(`${API_URL}/users/public/${id}`);
      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('Пользователь не найден');
        }
        throw new Error(`HTTP ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('[User API] Error getting public user:', error);
      throw error;
    }
  },
  
  // Получить текущего пользователя
  async getCurrentUser() {
    try {
      const userData = await fetchWithErrorHandling(`${API_URL}/users/me`);
      return this.formatUser(userData);
    } catch (error) {
      console.error('[User API] Error getting current user:', error);
      throw error;
    }
  },
  
  // === ПРИВАТНЫЕ МЕТОДЫ (требуют авторизации) ===
  
  // Получить пользователя по ID с авторизацией
  async getUserById(id) {
    try {
      const userData = await fetchWithErrorHandling(`${API_URL}/users/${id}`);
      return userData;
    } catch (error) {
      console.error('[User API] Error getting user by ID:', error);
      throw error;
    }
  },
  
  // Обновить профиль через JSON (с URL аватарки)
  async updateProfile(userData) {
    try {
      const updatedData = await fetchWithErrorHandling(`${API_URL}/users/me`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData)
      });
      return this.formatUser(updatedData);
    } catch (error) {
      console.error('[User API] Error updating profile:', error);
      throw error;
    }
  },
  
  // Обновить профиль с загрузкой аватарки (multipart/form-data)
  async updateProfileWithAvatar(formData) {
    try {
      const token = getToken();
      if (!token) {
        throw new Error('Требуется авторизация');
      }
      
      const headers = {
        'Authorization': `Bearer ${token}`
      };
      
      const response = await fetch(`${API_URL}/users/me`, {
        method: 'PUT',
        headers,
        body: formData
      });
      
      if (response.status === 401) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        window.location.href = '/login';
        throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
      }
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const updatedData = await response.json();
      return this.formatUser(updatedData);
    } catch (error) {
      console.error('[User API] Error updating profile with avatar:', error);
      throw error;
    }
  },
  
  // Загрузить аватарку (отдельный метод)
  async uploadAvatar(file) {
    try {
      const token = getToken();
      if (!token) {
        throw new Error('Требуется авторизация');
      }
      
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await fetch(`${API_URL}/users/me/avatar`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });
      
      if (response.status === 401) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        window.location.href = '/login';
        throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
      }
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const result = await response.json();
      return result;
    } catch (error) {
      console.error('[User API] Error uploading avatar:', error);
      throw error;
    }
  },
  
  // Удалить аватарку
  async deleteAvatar() {
    try {
      await fetchWithErrorHandling(`${API_URL}/users/me/avatar`, {
        method: 'DELETE'
      });
      return true;
    } catch (error) {
      console.error('[User API] Error deleting avatar:', error);
      throw error;
    }
  },
  
  // Получить всех пользователей (публичных)
  async getAllUsers() {
    try {
      const usersData = await fetchWithErrorHandling(`${API_URL}/users`);
      return Array.isArray(usersData) ? usersData.map(user => this.formatUser(user)) : [];
    } catch (error) {
      console.error('[User API] Error getting all users:', error);
      throw error;
    }
  },
  
  // Получить пользователя по username
  async getUserByUsername(username) {
    try {
      const userData = await fetchWithErrorHandling(`${API_URL}/users/username/${username}`);
      return this.formatUser(userData);
    } catch (error) {
      console.error('[User API] Error getting user by username:', error);
      throw error;
    }
  },
  
  // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
  
  // Создать FormData для обновления профиля с аватаркой
  createProfileFormData(data) {
    const formData = new FormData();
    
    if (data.avatarFile) {
      formData.append('avatarFile', data.avatarFile);
    }
    
    if (data.displayName !== undefined) {
      formData.append('displayName', data.displayName);
    }
    
    if (data.bio !== undefined) {
      formData.append('bio', data.bio);
    }
    
    if (data.isPublic !== undefined) {
      formData.append('isPublic', data.isPublic.toString());
    }
    
    return formData;
  },
  
  // Получить URL аватарки с fallback
  getAvatarUrl(user) {
    if (!user) return '/default-avatar.png';
    
    const avatarUrl = user.avatarUrl || user.pfp || user.avatar;
    if (!avatarUrl) return '/default-avatar.png';
    
    // Если URL уже полный
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
      return avatarUrl;
    }
    
    // Если относительный путь
    if (avatarUrl.startsWith('/uploads/images/') || avatarUrl.startsWith('/api/files/images/')) {
      return avatarUrl;
    }
    
    // Если просто имя файла
    if (avatarUrl.includes('.')) {
      return `/uploads/images/${avatarUrl}`;
    }
    
    return '/default-avatar.png';
  },
  
  // Получить полный URL для ресурсов
  getFullUrl(path) {
    if (!path) return '';
    
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path;
    }
    
    let finalPath = path;
    
    if (path.startsWith('/api/files/images/')) {
      const filename = path.split('/').pop();
      finalPath = `/uploads/images/${filename}`;
    }
    else if (path.startsWith('/uploads/')) {
      finalPath = path;
    }
    else if (path.startsWith('uploads/')) {
      finalPath = `/${path}`;
    }
    else if (!path.includes('/')) {
      finalPath = `/uploads/images/${path}`;
    }
    
    return `http://localhost:8081${finalPath}`;
  },
  
  // Форматировать данные пользователя для отображения
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
    
    // Если avatarUrl не полный, добавляем базовый URL
    if (formattedUser.avatarUrl && !formattedUser.avatarUrl.startsWith('http')) {
      formattedUser.avatarUrl = this.getFullUrl(formattedUser.avatarUrl);
    }
    
    return formattedUser;
  }
};

export default userApi;