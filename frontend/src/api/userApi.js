import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';
const USERS_BASE = `${API_URL}/users`;

// Стандартный JSON-запрос с авторизацией
const requestJson = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

// Multipart запрос (для аватарок) — Content-Type ставит браузер
const requestMultipart = async (url, options = {}) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(url, {
    credentials: 'include',
    method: options.method || 'POST',
    body: options.body,
    headers: {
      ...(token && { 'Authorization': `Bearer ${token}` }),
    },
  });
  
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP ${response.status}`);
  }
  
  return response.json();
};

// Форматирование UserDto с бэка
const formatUser = (user) => {
  if (!user) return null;
  return {
    id: user.id,
    username: user.username,
    displayName: user.displayName || user.username,
    email: user.email,
    bio: user.bio || '',
    avatarUrl: userApi.getAvatarUrl(user), // используем хелпер ниже
    isPublic: user.isPublic !== false,
    userRole: user.userRole,
    createdAt: user.createdAt,
  };
};

// Адаптация Page<UserDto> с бэка
const formatUserPage = (pageData) => {
  if (!pageData) return { content: [], totalElements: 0, totalPages: 0, last: true };
  return {
    content: (pageData.content || []).map(formatUser),
    totalElements: pageData.totalElements || 0,
    totalPages: pageData.totalPages || 0,
    number: pageData.number || 0,
    size: pageData.size || 20,
    last: pageData.last ?? true,
  };
};

export const userApi = {
  
  // === PUBLIC ENDPOINTS ===
  
  // Публичный профиль по ID (без авторизации)
  async getPublicUser(id) {
    const response = await fetch(`${USERS_BASE}/public/${id}`, { credentials: 'include' });
    if (!response.ok) {
      if (response.status === 404) throw new Error('Пользователь не найден или приватен');
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    return formatUser(data);
  },
  
  // === PRIVATE ENDPOINTS (требуют авторизации) ===
  
  // Текущий пользователь
  async getCurrentUser() {
    const data = await requestJson(`${USERS_BASE}/me`);
    return formatUser(data);
  },
  
  // Пользователь по ID (доступен если публичный или это текущий юзер)
  async getUserById(id) {
    const data = await requestJson(`${USERS_BASE}/${id}`);
    return formatUser(data);
  },
  
  // Пользователь по username
  async getUserByUsername(username) {
    const cleanName = username.replace(/^[@#]/, '');
    const data = await requestJson(`${USERS_BASE}/username/${encodeURIComponent(cleanName)}`);
    return formatUser(data);
  },
  
  // Универсальный getter: пробует приватный → публичный
  async getUser(userId, isAuthenticated = false) {
    try {
      if (isAuthenticated) {
        try {
          return await this.getUserById(userId);
        } catch (err) {
          if (err.message?.includes('404') || err.message?.includes('401')) {
            return await this.getPublicUser(userId);
          }
          throw err;
        }
      }
      return await this.getPublicUser(userId);
    } catch (error) {
      console.error('[User API] Error getting user:', error);
      throw error;
    }
  },
  
  // === UPDATE PROFILE ===
  
  // Обновление профиля (multipart: текст + файл)
  async updateProfile({ username, displayName, bio, isPublic, avatarFile, avatarUrl }) {
      const formData = new FormData();
      
      if (username !== undefined) formData.append('username', username);
      if (displayName !== undefined) formData.append('displayName', displayName);
      if (bio !== undefined) formData.append('bio', bio);
      if (isPublic !== undefined) formData.append('isPublic', String(isPublic));
      if (avatarUrl !== undefined) formData.append('avatarUrl', avatarUrl);
      if (avatarFile instanceof File) formData.append('avatarFile', avatarFile);
      
      console.log(' userApi.updateProfile — FormData:');
      for (let [key, value] of formData.entries()) {
          console.log(`  ${key}:`, value instanceof File ? `File(${value.name})` : value);
      }
      
      const result = await requestMultipart(`${USERS_BASE}/me`, {
          method: 'PUT',
          body: formData,
      });
       
      return {
          user: formatUser(result.user || result),
          newToken: result.newToken,
      };
  },
  
  // Хелпер для создания FormData
  createProfileFormData({ username, displayName, bio, isPublic, avatarFile, avatarUrl }) {
    const formData = new FormData();
    if (username !== undefined) formData.append('username', username);
    if (displayName !== undefined) formData.append('displayName', displayName);
    if (bio !== undefined) formData.append('bio', bio);
    if (isPublic !== undefined) formData.append('isPublic', String(isPublic));
    if (avatarUrl !== undefined) formData.append('avatarUrl', avatarUrl);
    if (avatarFile instanceof File) formData.append('avatarFile', avatarFile);
    return formData;
  },
  
  // Удаление аватарки
  async deleteAvatar() {
    await requestJson(`${USERS_BASE}/me/avatar`, { method: 'DELETE' });
    return true;
  },
  
  // === USER LIST & SEARCH ===
  
  // Все публичные пользователи (пагинация)
  async getAllPublicUsers({ page = 0, size = 20, sortBy = 'createdAt', direction = 'desc' } = {}) {
    const url = `${USERS_BASE}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    return formatUserPage(data);
  },
  
  // Поиск по username (для админов, но можно использовать и публично если бэк разрешит)
  async searchUsersByUsername(query, { page = 0, size = 20, sortBy = 'createdAt', direction = 'desc' } = {}) {
    const url = `${USERS_BASE}/all?search=${encodeURIComponent(query)}&page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    return formatUserPage(data);
  },
  
  // === ADMIN / MODERATOR ENDPOINTS ===
  
  // Пользователи по роли
  async getUsersByRole(role, { page = 0, size = 20 } = {}) {
    const url = `${USERS_BASE}/role/${role}?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatUserPage(data);
  },
  
  // Статистика по ролям
  async getRoleStatistics() {
    return await requestJson(`${USERS_BASE}/role/statistics`);
  },
  
  // Все админы
  async getAdmins() {
    const data = await requestJson(`${USERS_BASE}/admins`);
    return Array.isArray(data) ? data.map(formatUser) : [];
  },
  
  // Все пользователи (только ADMIN) с поиском
  async getAllUsersAdmin({ page = 0, size = 20, sortBy = 'createdAt', direction = 'desc', search = '' } = {}) {
    let url = `${USERS_BASE}/all?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    if (search?.trim()) {
      url += `&search=${encodeURIComponent(search.trim())}`;
    }
    const data = await requestJson(url);
    return formatUserPage(data);
  },
  
  // Изменение роли пользователя по ID
  async changeUserRole(userId, newRole) {
    const data = await requestJson(`${USERS_BASE}/${userId}/role?role=${newRole}`, { method: 'PUT' });
    return formatUser(data);
  },
  
  // Изменение роли по username
  async changeUserRoleByUsername(username, newRole) {
    const cleanName = username.replace(/^[@#]/, '');
    const data = await requestJson(`${USERS_BASE}/username/${encodeURIComponent(cleanName)}/role?role=${newRole}`, { method: 'PUT' });
    return formatUser(data);
  },
  
  // Массовое изменение ролей
  async bulkChangeRole(userIds, newRole) {
    const data = await requestJson(`${USERS_BASE}/role/bulk?role=${newRole}`, {
      method: 'POST',
      body: JSON.stringify(userIds),
    });
    return data; // { updatedCount, role, message }
  },
  
  // === ACCOUNT MANAGEMENT ===
  
  // Удаление аккаунта
  async deleteAccount() {
    await requestJson(`${USERS_BASE}/me`, { method: 'DELETE' });
    return true;
  },
  
  // === UTILS ===
  
  // Получение URL аватарки с нормализацией путей
  getAvatarUrl(user) {
    if (!user) return '/default-avatar.png';
    const avatarUrl = user.avatarUrl || user.pfp || user.avatar;
    if (!avatarUrl) return '/default-avatar.png';
    
    // Абсолютные URL возвращаем как есть
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
      return avatarUrl;
    }
    
    // Пути с бэка: /uploads/images/... или /api/files/images/...
    if (avatarUrl.startsWith('/uploads/') || avatarUrl.startsWith('/api/files/')) {
      return `http://localhost:8081${avatarUrl}`;
    }
    
    // Если просто имя файла — добавляем префикс
    if (avatarUrl.includes('.')) {
      return `http://localhost:8081/uploads/images/${avatarUrl}`;
    }
    
    return '/default-avatar.png';
  },
  
  // Полная сборка URL для любых путей с бэка
  getFullUrl(path) {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://')) return path;
    
    const BASE = 'http://localhost:8081';
    
    if (path.startsWith('/api/files/images/')) {
      const filename = path.split('/').pop();
      return `${BASE}/uploads/images/${filename}`;
    }
    if (path.startsWith('/uploads/')) {
      return `${BASE}${path}`;
    }
    if (path.startsWith('uploads/')) {
      return `${BASE}/${path}`;
    }
    if (!path.includes('/')) {
      return `${BASE}/uploads/images/${path}`;
    }
    
    return `${BASE}${path}`;
  },
  
  formatUser,
  formatUserPage,
  
  utils: {
    formatUser,
    formatUserPage,
    getAvatarUrl: (user) => userApi.getAvatarUrl(user),
    getFullUrl: (path) => userApi.getFullUrl(path),
  }
};

export default userApi;