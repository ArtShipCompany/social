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

// === ИНТЕРЦЕПТОРЫ ===
const requestInterceptors = [];
const responseInterceptors = [];

// Добавить интерцептор запроса
export const addRequestInterceptor = (interceptor) => {
  requestInterceptors.push(interceptor);
};

// Добавить интерцептор ответа
export const addResponseInterceptor = (interceptor) => {
  responseInterceptors.push(interceptor);
};

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

export const getAuthToken = () => authToken;

// === ОСНОВНАЯ ФУНКЦИЯ ДЛЯ ЗАПРОСОВ ===
async function fetchWithErrorHandling(url, options = {}) {
  console.log('🔵 [FETCH] Запрос к:', url);
  console.log('🔵 [FETCH] Тело запроса:', options.body);

  const finalOptions = {
    ...options,
    headers: {
      'Content-Type': 'application/json', 
      'Accept': 'application/json',       
      ...options.headers,
    },
  };
  
  console.log('🔵 [FETCH] Финальные заголовки:', finalOptions.headers);
  
  try {
    const response = await fetch(url, finalOptions);
    console.log('🟢 [FETCH] Ответ получен, статус:', response.status);
    console.log('🟢 [FETCH] Заголовки:', Object.fromEntries(response.headers.entries()));
    
    
    const text = await response.text();
    console.log('🟢 [FETCH] Сырой текст ответа:', text);
    
    let data;
    try {
      data = text ? JSON.parse(text) : {};
      console.log('🟢 [FETCH] JSON распарсен:', data);
    } catch (jsonError) {
      console.error('❌ [FETCH] Ошибка парсинга JSON:', jsonError);
      data = { rawText: text };
    }
    
    
    if (!response.ok) {
      console.error('❌ [FETCH] HTTP ошибка:', response.status);
      
      
      let errorMessage = `HTTP ${response.status}`;
      if (data && typeof data === 'object') {
        errorMessage = data.message || data.error || data.errorMessage || errorMessage;
      }
      
      const error = new ApiError(errorMessage, response.status, data);
      throw error;
    }
    
    
    console.log('✅ [FETCH] Запрос успешен, возвращаю данные');
    return data;
    
  } catch (error) {
    console.error('❌ [FETCH] Ошибка запроса:', error);
    
    
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

// === ИНТЕРЦЕПТОР ДЛЯ АВТООБНОВЛЕНИЯ TOKEN ===
let isRefreshing = false;
let refreshSubscribers = [];

// Функция для подписки на обновление токена
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback);
}

// Функция для оповещения подписчиков
function onTokenRefreshed(token) {
  refreshSubscribers.forEach(callback => callback(token));
  refreshSubscribers = [];
}

// Интерцептор для автоматического обновления токена при 401 ошибке
addResponseInterceptor(async (response) => {
  if (response.status === 401 && authToken && !response.url.includes('/auth/refresh')) {
    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          console.log('[API] Auto-refreshing token...');
          const refreshData = await authApi.refreshToken(refreshToken);
          
          if (refreshData.accessToken) {
            setAuthToken(refreshData.accessToken);
            localStorage.setItem('refreshToken', refreshData.refreshToken);
            onTokenRefreshed(refreshData.accessToken);
          }
        }
      } catch (refreshError) {
        console.error('[API] Token refresh failed:', refreshError);
        // Если не удалось обновить токен, очищаем авторизацию
        setAuthToken(null);
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        // Можно редиректнуть на логин страницу
        window.dispatchEvent(new CustomEvent('auth-expired'));
      } finally {
        isRefreshing = false;
      }
    }
    
    // Возвращаем промис для ожидания обновления токена
    return new Promise((resolve) => {
      subscribeTokenRefresh((newToken) => {
        // Здесь можно повторить оригинальный запрос с новым токеном
        resolve(response); // Пока просто возвращаем оригинальный response
      });
    });
  }
  return response;
});

// === API МЕТОДЫ ===
export const authApi = {
  // Регистрация
  async register(userData) {
    try {
      const response = await fetchWithErrorHandling(`${API_URL}/auth/register`, {
        method: 'POST',
        body: JSON.stringify({
          username: userData.login || userData.username, // Поддержка обоих вариантов
          email: userData.email,
          password: userData.password
        }),
      });
      
      console.log('[API] Registration successful:', response);
      return response;
    } catch (error) {
      console.error('[API] Registration failed:', error);
      throw error;
    }
  },

  // Логин 
   async login(credentials) {
    console.log('🔵 [AUTH API] Начало login()');
    console.log('🔵 [AUTH API] credentials:', credentials);
    
    try {
      const username = credentials.username || credentials.identifier || credentials.login || '';
      console.log('🔵 [AUTH API] username extracted:', username);
      
      const response = await fetchWithErrorHandling(`${API_URL}/auth/login`, {
        method: 'POST',
        body: JSON.stringify({
          username: username.trim(),
          password: credentials.password || ''
        }),
      });
      
      console.log('🟢 [AUTH API] Успешный ответ от сервера:', response);
      console.log('🟢 [AUTH API] accessToken exists:', !!response.accessToken);
      console.log('🟢 [AUTH API] refreshToken exists:', !!response.refreshToken);
      console.log('🟢 [AUTH API] user exists:', !!response.user);
      
      // Проверяем структуру ответа
      if (!response.accessToken) {
        console.warn('⚠️ [AUTH API] Внимание: accessToken отсутствует в ответе');
      }
      
      // Сохраняем токены
      if (response.accessToken) {
        setAuthToken(response.accessToken);
        console.log('✅ [AUTH API] accessToken сохранен');
      }
      
      if (response.refreshToken) {
        localStorage.setItem('refreshToken', response.refreshToken);
        console.log('✅ [AUTH API] refreshToken сохранен');
      }
      
      if (response.user) {
        localStorage.setItem('user', JSON.stringify(response.user));
        console.log('✅ [AUTH API] user сохранен:', response.user);
      }
      
      if (response.expiresIn) {
        localStorage.setItem('tokenExpiry', (Date.now() + response.expiresIn).toString());
        console.log('✅ [AUTH API] expiresIn сохранен');
      }
      
      console.log('✅ [AUTH API] login() завершен успешно');
      return response;
      
    } catch (error) {
      console.error('❌ [AUTH API] Ошибка в login():', error);
      console.error('❌ [AUTH API] Stack trace:', error.stack);
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
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      localStorage.removeItem('tokenExpiry');
      
      console.log('[API] Logout successful');
      return { success: true };
    } catch (error) {
      console.error('[API] Logout failed:', error);
      // Все равно очищаем локальное хранилище даже при ошибке
      setAuthToken(null);
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
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
      }
      
      console.log('[API] Token refreshed successfully');
      return response;
    } catch (error) {
      console.error('[API] Token refresh failed:', error);
      throw error;
    }
  },

  // Проверка текущего пользователя
  async getCurrentUser() {
    try {
      // Если у нас есть пользователь в localStorage и токен еще не истек
      const userStr = localStorage.getItem('user');
      const tokenExpiry = localStorage.getItem('tokenExpiry');
      
      if (userStr && authToken && tokenExpiry && Date.now() < parseInt(tokenExpiry)) {
        return JSON.parse(userStr);
      }
      
      // Можно добавить endpoint для проверки токена на сервере
      // Например: /api/auth/me
      // const response = await fetchWithErrorHandling(`${API_URL}/auth/me`);
      // return response;
      
      return null;
    } catch (error) {
      console.error('[API] Get current user failed:', error);
      return null;
    }
  },

  };



// Проверка авторизации
export const isAuthenticated = () => {
  const token = getAuthToken();
  const tokenExpiry = localStorage.getItem('tokenExpiry');
  
  if (!token) return false;
  
  // Проверяем не истек ли токен
  if (tokenExpiry && Date.now() > parseInt(tokenExpiry)) {
    console.log('[Auth] Token expired');
    return false;
  }
  
  return true;
};

// Получение текущего пользователя
export const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  } catch {
    return null;
  }
};

// Создание заголовков авторизации
export const getAuthHeaders = () => {
  const token = getAuthToken();
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

// === ЭКСПОРТ ===
export default authApi;