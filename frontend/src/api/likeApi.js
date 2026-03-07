const API_URL = 'http://localhost:8081/api';

// Функция для получения токена
const getToken = () => {
    return localStorage.getItem('accessToken');
};

export const likeApi = {
    // Добавить лайк
    async addLike(userId, artId) {
        try {
            const token = getToken();
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch(`${API_URL}/likes/user/${userId}/art/${artId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('[API] Error adding like:', error);
            throw error;
        }
    },

    // Удалить лайк
    async removeLike(userId, artId) {
        try {
            const token = getToken();
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch(`${API_URL}/likes/user/${userId}/art/${artId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            return true;
        } catch (error) {
            console.error('[API] Error removing like:', error);
            throw error;
        }
    },

    // Проверить лайк
    async isLiked(userId, artId) {
        try {
            const token = getToken();
            if (!token) {
                return false; // Если нет токена, считаем что не лайкнуто
            }

            const response = await fetch(`${API_URL}/likes/user/${userId}/art/${artId}/exists`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            
            if (!response.ok) {
                if (response.status === 401) {
                    return false; // Если не авторизован, считаем что не лайкнуто
                }
                throw new Error(`HTTP ${response.status}`);
            }
            
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('[API] Error checking like:', error);
            return false; // В случае ошибки считаем что не лайкнуто
        }
    },

    // Получить количество лайков арта - ИСПРАВЛЕНО
    async getLikeCountByArt(artId) {
        try {
            const token = getToken();
            const headers = {};
            
            // Добавляем токен только если он есть
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await fetch(`${API_URL}/likes/art/${artId}/count`, {
                headers
            });
            
            if (!response.ok) {
                // Если 401 и нет токена, пробуем без токена
                if (response.status === 401 && !token) {
                    console.log('Unauthorized for like count, returning default');
                    return 0; // Возвращаем 0 если не авторизован
                }
                throw new Error(`HTTP ${response.status}`);
            }
            
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('[API] Error getting like count:', error);
            // В случае ошибки возвращаем 0 или исходное количество
            return 0;
        }
    },

    // Получить лайки арта
    async getLikesByArt(artId) {
        try {
            const token = getToken();
            const headers = {};
            
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await fetch(`${API_URL}/likes/art/${artId}`, {
                headers
            });
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('[API] Error getting likes:', error);
            throw error;
        }
    },
};

export default likeApi;