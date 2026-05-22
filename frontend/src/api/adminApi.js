import { fetchWithErrorHandling, API_URL } from './authApi';

export const adminApi = {
  // Получение всех пользователей
  getAllUsers: async (page = 0, size = 20, sortBy = 'createdAt', direction = 'desc', search = '') => {
    let url = `${API_URL}/users/all?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    if (search && search.trim()) {
      url += `&search=${encodeURIComponent(search.trim())}`;
    }
    console.log('Admin API - getAllUsers URL:', url);
    return fetchWithErrorHandling(url);
  },
  
  // Изменение роли
  changeUserRole: async (userId, role) => {
    const response = await fetchWithErrorHandling(`${API_URL}/users/${userId}/role?role=${role}`, {
      method: 'PUT',
    });
    return response; // Должен вернуть обновленного пользователя
  },
  
  // Статистика по ролям
  getRoleStatistics: async () => {
    const url = `${API_URL}/users/role/statistics`;
    console.log('Admin API - getRoleStatistics URL:', url);
    return fetchWithErrorHandling(url);
  },
  
  // Получение пользователей по роли
  getUsersByRole: async (role, page = 0, size = 20) => {
    const url = `${API_URL}/users/role/${role}?page=${page}&size=${size}`;
    console.log('Admin API - getUsersByRole URL:', url);
    return fetchWithErrorHandling(url);
  },
  
  // Массовое изменение ролей
  bulkChangeRole: async (userIds, role) => {
    const url = `${API_URL}/users/role/bulk?role=${role}`;
    console.log('Admin API - bulkChangeRole URL:', url);
    return fetchWithErrorHandling(url, {
      method: 'POST',
      body: JSON.stringify(userIds),
    });
  },
  
  // Поиск пользователей
  searchUsers: async (query, page = 0, size = 20) => {
    const url = `${API_URL}/users/all?search=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    console.log('Admin API - searchUsers URL:', url);
    return fetchWithErrorHandling(url);
  },
  
  // Удаление пользователя (только для админа)
  deleteUser: async (userId) => {
    const url = `${API_URL}/users/${userId}`;
    console.log('Admin API - deleteUser URL:', url);
    return fetchWithErrorHandling(url, {
      method: 'DELETE',
    });
  },
  
  // Получение деталей пользователя для админа
  getUserDetails: async (userId) => {
    const url = `${API_URL}/users/${userId}`;
    console.log('Admin API - getUserDetails URL:', url);
    return fetchWithErrorHandling(url);
  },
};