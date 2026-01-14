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

// === ОБРАБОТЧИК TOKEN ===
let authToken = localStorage.getItem('accessToken') || null;

export const setAuthToken = (token) => {
  authToken = token;
  if (token) {
    localStorage.setItem('accessToken', token);
  } else {
    localStorage.removeItem('accessToken');
  }
};

export const getAuthToken = () => {
  const token = localStorage.getItem('accessToken');
  console.log('🔑 getAuthToken called, token exists:', !!token);
  return token;
};

// === ОСНОВНАЯ ФУНКЦИЯ ДЛЯ ЗАПРОСОВ ===
async function fetchWithErrorHandling(url, options = {}) {
  const finalOptions = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };
  
  // Добавляем токен если есть
  const token = getAuthToken();
  if (token && !url.includes('/auth/')) {
    finalOptions.headers['Authorization'] = `Bearer ${token}`;
  }
  
  try {
    const response = await fetch(url, finalOptions);
    
    let data;
    try {
      const text = await response.text();
      data = text ? JSON.parse(text) : {};
    } catch (jsonError) {
      data = {};
    }
    
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      if (data && typeof data === 'object') {
        errorMessage = data.message || data.error || errorMessage;
      }
      
      const error = new ApiError(errorMessage, response.status, data);
      throw error;
    }
    
    return data;
    
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    
    const apiError = new ApiError(
      error.message || 'Network error',
      0,
      { originalError: error }
    );
    throw apiError;
  }
}

// === API МЕТОДЫ ===
export const authApi = {
  // Регистрация
  async register(userData) {
    try {
        const response = await fetchWithErrorHandling(`${API_URL}/auth/register`, {
            method: 'POST',
            body: JSON.stringify({
                username: userData.login || userData.username,
                email: userData.email,
                password: userData.password
            }),
        });
        
        console.log('✅ Registration response:', response);
        
        // Некоторые бэкенды возвращают пользователя сразу после регистрации
        if (response.user) {
            localStorage.setItem('user', JSON.stringify(response.user));
        }
        
        return response;
    } catch (error) {
        console.error('❌ Registration API error:', error);
        throw error;
    }
},

  // Логин 
  async login(credentials) {
    try {
      const username = credentials.username || credentials.identifier || credentials.login || '';
      
      const response = await fetchWithErrorHandling(`${API_URL}/auth/login`, {
        method: 'POST',
        body: JSON.stringify({
          username: username.trim(),
          password: credentials.password || ''
        }),
      });
      
      // Сохраняем данные
      if (response.accessToken) {
        setAuthToken(response.accessToken);
        localStorage.setItem('accessToken', response.accessToken);
      }
      
      if (response.refreshToken) {
        localStorage.setItem('refreshToken', response.refreshToken);
      }
      
      if (response.user) {
        localStorage.setItem('user', JSON.stringify(response.user));
      }
      
      if (response.expiresIn) {
        localStorage.setItem('tokenExpiry', (Date.now() + response.expiresIn).toString());
      }
      
      return response;
      
    } catch (error) {
      throw error;
    }
  },

  // Выход
  async logout(refreshToken = null) {
    try {
      const tokenToUse = refreshToken || localStorage.getItem('refreshToken');
      
      if (tokenToUse) {
        await fetchWithErrorHandling(`${API_URL}/auth/logout`, {
          method: 'POST',
          body: JSON.stringify({ refreshToken: tokenToUse }),
        });
      }
      
      // Очищаем локальное хранилище
      setAuthToken(null);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      localStorage.removeItem('tokenExpiry');
      
      return { success: true };
    } catch (error) {
      // Все равно очищаем локальное хранилище даже при ошибке
      setAuthToken(null);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      localStorage.removeItem('tokenExpiry');
      throw error;
    }
  },

  // Обновление токена
  async refreshToken(refreshToken) {
    try {
      const response = await fetchWithErrorHandling(`${API_URL}/auth/refresh`, {
        method: 'POST',
        body: JSON.stringify({ refreshToken }),
      });
      
      if (response.accessToken) {
        setAuthToken(response.accessToken);
        localStorage.setItem('accessToken', response.accessToken);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
      }
      
      return response;
    } catch (error) {
      throw error;
    }
  },
};

// Проверка авторизации
export const isAuthenticated = () => {
  const token = localStorage.getItem('accessToken');
  const tokenExpiry = localStorage.getItem('tokenExpiry');
  
  if (!token) {
    console.log('🔐 isAuthenticated: no token');
    return false;
  }
  
  // Проверяем не истек ли токен
  if (tokenExpiry && Date.now() > parseInt(tokenExpiry)) {
    console.log('🔐 isAuthenticated: token expired');
    return false;
  }
  
  console.log('🔐 isAuthenticated: user is authenticated');
  return true;
};

// Получение текущего пользователя
export const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      console.log('👤 getCurrentUser: no user in localStorage');
      return null;
    }
    
    const user = JSON.parse(userStr);
    console.log('👤 getCurrentUser: user found', user.username);
    return user;
  } catch (error) {
    console.error('👤 getCurrentUser error:', error);
    return null;
  }
};

export default authApi;