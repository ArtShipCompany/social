const API_URL = 'http://localhost:8081/api';

// === КЛАСС ДЛЯ КАСТОМНЫХ ОШИБОК ===
class ApiError extends Error {
  constructor(message, status, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

// === TOKEN MANAGEMENT (только access token) ===
let authToken = localStorage.getItem('accessToken') || null;

export const setAuthToken = (token) => {
  authToken = token;
  if (token) {
    localStorage.setItem('accessToken', token);
  } else {
    localStorage.removeItem('accessToken');
  }
};

export const getAuthToken = () => localStorage.getItem('accessToken');

export const clearAuthStorage = () => {
  setAuthToken(null);
  localStorage.removeItem('user');
  localStorage.removeItem('tokenExpiry');
};

// === ОСНОВНАЯ ФУНКЦИЯ ДЛЯ ЗАПРОСОВ ===
export async function fetchWithErrorHandling(url, options = {}) {
  const finalOptions = {
    ...options,
    credentials: 'include', // 🔥 КРИТИЧНО: отправляем/получаем куки
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };
  
  // Добавляем access token в заголовок, если есть
  const token = getAuthToken();
  if (token) {
    finalOptions.headers['Authorization'] = `Bearer ${token}`;
  }
  
  try {
    const response = await fetch(url, finalOptions);
    
    let data;
    try {
      const text = await response.text();
      data = text ? JSON.parse(text) : {};
    } catch {
      data = {};
    }
    
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      if (data?.message || data?.error) {
        errorMessage = data.message || data.error;
      }
      throw new ApiError(errorMessage, response.status, data);
    }
    
    return data;
    
  } catch (error) {
    if (error instanceof ApiError) throw error;
    
    throw new ApiError(
      error.message || 'Network error',
      0,
      { originalError: error }
    );
  }
}

// === ФУНКЦИЯ ОБНОВЛЕНИЯ ТОКЕНА (внутренняя) ===
async function handleTokenRefresh() {
  try {
    const response = await fetchWithErrorHandling(`${API_URL}/auth/refresh`, {
      method: 'POST',
    });
    
    if (response.accessToken) {
      setAuthToken(response.accessToken);
      return true;
    }
    return false;
  } catch (error) {
    console.warn('❌ Token refresh failed:', error.message);
    clearAuthStorage();
    return false;
  }
}

// === WRAPPER С АВТО-РЕФРЕШЕМ ===
async function fetchWithAutoRefresh(url, options = {}, retryCount = 0) {
  try {
    return await fetchWithErrorHandling(url, options);
  } catch (error) {
    // 🔁 Если 401 и ещё не пробовали рефрешить — пытаемся обновить токен
    if (error.status === 401 && retryCount === 0 && !url.includes('/auth/')) {
      console.log('🔄 Access token expired, trying refresh...');
      const refreshed = await handleTokenRefresh();
      
      if (refreshed) {
        // Повторяем исходный запрос с новым токеном
        return fetchWithAutoRefresh(url, options, 1);
      }
    }
    throw error;
  }
}

// === API МЕТОДЫ ===
export const authApi = {
  async register(userData) {
    const response = await fetchWithErrorHandling(`${API_URL}/auth/register`, {
      method: 'POST',
      body: JSON.stringify({
        username: userData.login || userData.username,
        email: userData.email,
        password: userData.password
      }),
    });
    
    if (response.user) {
      localStorage.setItem('user', JSON.stringify(response.user));
    }
    return response;
  },

  async verifyEmail(token) {
    return fetchWithErrorHandling(`${API_URL}/auth/verify?token=${token}`, {
      method: 'GET',
    });
  },

  async resendVerification(email) {
    return fetchWithErrorHandling(`${API_URL}/auth/resend-verification`, {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },

  async checkVerificationStatus(email) {
    return fetchWithErrorHandling(`${API_URL}/auth/requires-verification?email=${encodeURIComponent(email)}`, {
      method: 'GET',
    });
  },

  async login(credentials) {
    const username = credentials.username || credentials.identifier || credentials.login || '';
    
    const response = await fetchWithErrorHandling(`${API_URL}/auth/login`, {
      method: 'POST',
      body: JSON.stringify({
        username: username.trim(),
        password: credentials.password || ''
      }),
    });
    
    // 🔥 Сохраняем только access token (refresh теперь в HttpOnly cookie)
    if (response.accessToken) {
      setAuthToken(response.accessToken);
    }
    
    if (response.user) {
      localStorage.setItem('user', JSON.stringify(response.user));
    }
    
    if (response.expiresIn) {
      localStorage.setItem('tokenExpiry', (Date.now() + response.expiresIn).toString());
    }
    
    return response;
  },

  async logout() {
    try {
      // 🔥 Просто вызываем эндпоинт — бэк сам прочитает куку и очистит её
      await fetchWithErrorHandling(`${API_URL}/auth/logout`, {
        method: 'POST',
      });
    } finally {
      // Всегда чистим локальное хранилище
      clearAuthStorage();
    }
    return { success: true };
  },

  // 🔥 Публичный метод рефреша (если нужно вызвать вручную)
  async refreshToken() {
    return handleTokenRefresh();
  },

  // 🔥 Метод для защищённых запросов с авто-рефрешем
  async fetchProtected(url, options = {}) {
    return fetchWithAutoRefresh(url, options);
  }
};

// === УТИЛИТЫ ===
export const isAuthenticated = () => {
  const token = getAuthToken();
  const tokenExpiry = localStorage.getItem('tokenExpiry');
  
  if (!token) return false;
  if (tokenExpiry && Date.now() > parseInt(tokenExpiry)) return false;
  return true;
};

export const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  } catch {
    return null;
  }
};

export default authApi;