const API_URL = 'http://localhost:8081/api';

const getToken = () => {
  return localStorage.getItem('accessToken');
};

const formatAuthor = (authorData, fallbackId, fallbackName) => {
  if (!authorData || typeof authorData !== 'object') {
    return {
      id: fallbackId || 'unknown',
      displayName: fallbackName || 'Неизвестный автор',
      pfp: '/default-avatar.png'
    };
  }
  
  // Получаем имя один раз
  let name = 'Неизвестный автор';
  
  if (authorData.displayName && authorData.displayName !== 'Неизвестный автор') {
    name = authorData.displayName;
  } else if (authorData.nickname) {
    name = authorData.nickname;
  } else if (authorData.name) {
    name = authorData.name;
  } else if (authorData.username) {
    name = authorData.username;
  } else if (fallbackName && fallbackName !== 'Неизвестный автор') {
    name = fallbackName;
  }
  
  // Убираем возможное дублирование
  const cleanName = (nameStr) => {
    if (typeof nameStr !== 'string') return 'Неизвестный автор';
    
    // Если строка состоит из двух одинаковых половинок
    const half = Math.floor(nameStr.length / 2);
    if (nameStr.substring(0, half) === nameStr.substring(half)) {
      return nameStr.substring(0, half);
    }
    
    return nameStr;
  };
  
  return {
    id: authorData.id || authorData.userId || fallbackId || 'unknown',
    displayName: cleanName(name), // Очищаем от дублирования
    pfp: authorData.pfp || authorData.avatar || authorData.profilePicture || '/default-avatar.png'
  };
};

// Функция для форматирования тегов
const formatTags = (tags) => {
  if (!tags) return '#no-tags';
  
  if (Array.isArray(tags)) {
    const tagStrings = tags.map(tag => {
      if (typeof tag === 'string') {
        const trimmed = tag.trim();
        return trimmed ? (trimmed.startsWith('#') ? trimmed : `#${trimmed}`) : '';
      }
      if (tag && typeof tag === 'object') {
        const tagName = tag.name || tag.tag || tag.id;
        if (tagName && typeof tagName === 'string') {
          const trimmed = tagName.trim();
          return trimmed ? (trimmed.startsWith('#') ? trimmed : `#${trimmed}`) : '';
        }
      }
      return '';
    }).filter(tag => tag !== '');
    
    return tagStrings.join(' ') || '#no-tags';
  }
  
  if (typeof tags === 'string') {
    const trimmed = tags.trim();
    return trimmed ? (trimmed.startsWith('#') ? trimmed : `#${trimmed}`) : '#no-tags';
  }
  
  return '#no-tags';
};

// Функция для получения корректного URL изображения
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return '/default-art.jpg';
  
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  
  // ВСЕГДА используем /uploads/images/ вместо /api/files/images/
  if (imageUrl.startsWith('/api/files/images/')) {
    // Конвертируем в /uploads/images/
    const filename = imageUrl.split('/').pop();
    return `/uploads/images/${filename}`;
  }
  
  if (imageUrl.startsWith('/uploads/images/')) {
    return imageUrl;
  }
  
  if (imageUrl.startsWith('/')) {
    return imageUrl;
  }
  
  if (imageUrl && imageUrl.includes('.')) {
    return `/uploads/images/${imageUrl}`;
  }
  
  return '/default-art.jpg';
};

// Функция для получения альтернативного URL изображения
const getAlternativeImageUrl = (imageUrl) => {
  if (!imageUrl) return null;
  
  // Если текущий URL - /uploads/images/, возвращаем /api/files/images/
  if (imageUrl.startsWith('/uploads/images/')) {
    return imageUrl.replace('/uploads/images/', '/api/files/images/');
  }
  
  // Если текущий URL - /api/files/images/, возвращаем /uploads/images/
  if (imageUrl.startsWith('/api/files/images/')) {
    return imageUrl.replace('/api/files/images/', '/uploads/images/');
  }
  
  return null;
};

// Функция для обработки ошибок загрузки изображения
const handleImageError = (event, currentImageUrl) => {
  console.error('Image load error for URL:', currentImageUrl);
  
  const alternativeUrl = getAlternativeImageUrl(currentImageUrl);
  
  if (alternativeUrl) {
    console.log('Trying alternative URL:', alternativeUrl);
    event.target.src = alternativeUrl;
    return true; // Альтернативный URL установлен
  }
  
  // Если ничего не помогло
  event.target.src = '/default-art.jpg';
  console.log('Falling back to default image');
  return false; // Использован дефолтный
};

// Функция для предзагрузки изображения
const preloadImage = (imageUrl) => {
  return new Promise((resolve, reject) => {
    if (!imageUrl || imageUrl === '/default-art.jpg') {
      resolve('/default-art.jpg');
      return;
    }
    
    const img = new Image();
    img.src = imageUrl;
    
    img.onload = () => {
      console.log('Image preloaded successfully:', imageUrl);
      resolve(imageUrl);
    };
    
    img.onerror = () => {
      console.error('Failed to preload image:', imageUrl);
      
      // Пробуем альтернативный URL
      const alternativeUrl = getAlternativeImageUrl(imageUrl);
      if (alternativeUrl) {
        console.log('Trying alternative URL in preload:', alternativeUrl);
        const altImg = new Image();
        altImg.src = alternativeUrl;
        
        altImg.onload = () => {
          console.log('Alternative image loaded:', alternativeUrl);
          resolve(alternativeUrl);
        };
        
        altImg.onerror = () => {
          console.error('Alternative URL also failed');
          reject(new Error('Both image URLs failed'));
        };
      } else {
        reject(new Error('Image loading failed'));
      }
    };
  });
};

// Функция для форматирования арта
const formatArt = (art) => {
  if (!art) return null;
  
  return {
    ...art,
    imageUrl: getImageUrl(art.imageUrl || art.image || art.imagePath || ''),
    tags: formatTags(art.tags),
    author: formatAuthor(art.author, art.authorId, art.authorName)
  };
};

// Функция для форматирования массива артов
const formatArtsArray = (arts) => {
  if (!arts || !Array.isArray(arts)) return [];
  
  return arts.map(art => formatArt(art));
};

export const artApi = {
  // Получить публичные арты (для неавторизованных)
  async getPublicArts(page = 0, size = 30) {
    try {
      const response = await fetch(
        `${API_URL}/arts/public?page=${page}&size=${size}&sortBy=createdAt&direction=desc`
      );
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      
      const data = await response.json();
      
      // Форматируем данные артов
      if (data.content && Array.isArray(data.content)) {
        data.content = formatArtsArray(data.content);
      }
      
      return data;
    } catch (error) {
      console.error('[API] Error fetching public arts:', error);
      throw error;
    }
  },

  // Получить ленту подписок (для авторизованных)
  async getFeedArts(page = 0, size = 30) {
    try {
      const token = getToken();
      
      if (!token) {
        console.warn('[API] No token found for feed request');
        throw new Error('Unauthorized: No authentication token');
      }
      
      console.log('[API] Fetching feed arts with token:', token.substring(0, 20) + '...');
      
      const response = await fetch(
        `${API_URL}/arts/feed?page=${page}&size=${size}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log('[API] Feed response status:', response.status);
      
      if (response.status === 401) {
        throw new Error('Unauthorized: Invalid or expired token');
      }
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('[API] Feed data received:', data);
      
      // Форматируем данные артов
      if (data.content && Array.isArray(data.content)) {
        data.content = formatArtsArray(data.content);
      }
      
      return data;
    } catch (error) {
      console.error('[API] Error fetching feed arts:', error);
      throw error;
    }
  },

  // Поиск по тегам
  async searchByTag(tagName, page = 0, size = 30) {
    try {
      // Убираем # если есть
      const cleanTagName = tagName.replace(/^#/, '');
      
      const response = await fetch(
        `${API_URL}/arts/tag/${cleanTagName}?page=${page}&size=${size}`
      );
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      
      const data = await response.json();
      
      // Форматируем данные артов
      if (data.content && Array.isArray(data.content)) {
        data.content = formatArtsArray(data.content);
      }
      
      return data;
    } catch (error) {
      console.error('[API] Error searching by tag:', error);
      throw error;
    }
  },

  async getArtById(artId) {
    try {
      const token = getToken();
      const headers = {
        'Content-Type': 'application/json'
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      console.log(`[API] Fetching art ${artId}`);
      const response = await fetch(`${API_URL}/arts/${artId}`, {
        headers
      });
      
      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('Арт не найден');
        }
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      
      // Форматируем арт
      return formatArt(data);
    } catch (error) {
      console.error('[API] Error fetching art by ID:', error);
      throw error;
    }
  },

  // Обновить арт
  async updateArt(artId, artData) {
    try {
      const token = getToken();
      if (!token) {
        throw new Error('Требуется авторизация');
      }
      
      const response = await fetch(`${API_URL}/arts/${artId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(artData)
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      return formatArt(data);
    } catch (error) {
      console.error('[API] Error updating art:', error);
      throw error;
    }
  },


    async getArtsByAuthor(userId, page = 0, size = 30) {
    try {
        const token = getToken();
        console.log(`[API] Fetching arts by author ${userId}`);
        console.log(`[API] Token available: ${!!token}`);
        
        if (!token) {
        throw new Error('Требуется авторизация для просмотра артов пользователя');
        }
        
        const response = await fetch(
        `${API_URL}/arts/author/${userId}?page=${page}&size=${size}`,
        {
            headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
            }
        }
        );
        
        console.log(`[API] Response status: ${response.status}`);
        console.log(`[API] Response headers:`, [...response.headers.entries()]);
        
        if (response.status === 401) {
        // Проверяем, не истек ли токен
        console.error('[API] Token might be expired or invalid');
        // Можно попробовать обновить токен здесь
        }
        
        if (!response.ok) {
        const errorText = await response.text();
        console.error('[API] Error response:', errorText);
        throw new Error(`HTTP ${response.status}: ${response.statusText || 'Неизвестная ошибка'}`);
        }
        
        const data = await response.json();
        console.log(`[API] Arts data received:`, data);
        
        // Форматируем данные артов
        if (data.content && Array.isArray(data.content)) {
        console.log(`[API] Formatting ${data.content.length} arts`);
        data.content = formatArtsArray(data.content);
        }
        
        return data;
    } catch (error) {
        console.error('[API] Error fetching arts by author:', error);
        throw error;
    }
    },

  // Поиск по названию
  async searchByTitle(title, page = 0, size = 30) {
    try {
      const response = await fetch(
        `${API_URL}/arts/search?title=${encodeURIComponent(title)}&page=${page}&size=${size}`
      );
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      
      const data = await response.json();
      
      // Форматируем данные артов
      if (data.content && Array.isArray(data.content)) {
        data.content = formatArtsArray(data.content);
      }
      
      return data;
    } catch (error) {
      console.error('[API] Error searching by title:', error);
      throw error;
    }
  },


  async createArt(artData, imageFile = null) {
    try {
        const token = getToken();
        if (!token) {
        throw new Error('Требуется авторизация для создания арта');
        }
        
        console.log('[Art API] Creating new art');
        console.log('[Art API] Art data:', artData);
        console.log('[Art API] Has image file:', !!imageFile);
        
        // Создаем FormData для отправки с файлом
        const formData = new FormData();
        
        // Добавляем обязательные поля
        if (!artData.title || artData.title.trim() === '') {
        throw new Error('Заголовок арта обязателен');
        }
        
        formData.append('title', artData.title.trim());
        
        // Добавляем опциональные поля
        if (artData.description !== undefined) {
        formData.append('description', artData.description);
        }
        
        if (artData.isPublic !== undefined) {
        formData.append('isPublic', artData.isPublic.toString());
        } else {
        formData.append('isPublic', 'true'); // По умолчанию публичный
        }
        
        // Добавляем файл изображения (обязательный)
        if (!imageFile) {
        throw new Error('Изображение арта обязательно');
        }
        
        // Проверяем тип файла
        if (!imageFile.type.startsWith('image/')) {
        throw new Error('Загруженный файл не является изображением');
        }
        
        // Проверяем размер файла (макс 10MB)
        if (imageFile.size > 10 * 1024 * 1024) {
        throw new Error('Размер файла превышает 10MB');
        }
        
        formData.append('imageFile', imageFile);
        
        console.log('[Art API] Sending create request...');
        
        const response = await fetch(`${API_URL}/arts`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            // Не устанавливаем Content-Type для FormData, браузер сам добавит
        },
        body: formData
        });
        
        console.log('[Art API] Create response status:', response.status);
        
        if (!response.ok) {
        let errorText = await response.text();
        console.error('[Art API] Create error response:', errorText);
        
        // Пробуем распарсить JSON для получения сообщения об ошибке
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(errorJson.message || `HTTP ${response.status}: ${response.statusText}`);
        } catch {
            throw new Error(`HTTP ${response.status}: ${response.statusText || 'Не удалось создать арт'}`);
        }
        }
        
        const data = await response.json();
        console.log('[Art API] Create successful:', data);
        
        // Форматируем созданный арт
        return formatArt(data);
        
    } catch (error) {
        console.error('[Art API] Error creating art:', error);
        throw error;
    }
    },

  async updateArt(artId, artData, imageFile = null) {
    try {
        const token = getToken();
        if (!token) {
        throw new Error('Требуется авторизация');
        }
        
        console.log('[Art API] Updating art:', artId, artData, 'has file:', !!imageFile);
        
        // Создаем FormData
        const formData = new FormData();
        
        // Добавляем текстовые поля
        if (artData.title !== undefined) {
        formData.append('title', artData.title);
        }
        if (artData.description !== undefined) {
        formData.append('description', artData.description);
        }
        if (artData.isPublic !== undefined) {
        formData.append('isPublic', artData.isPublic.toString());
        }
        
        // Добавляем файл если есть
        if (imageFile) {
        formData.append('imageFile', imageFile);
        }
        
        const response = await fetch(`${API_URL}/arts/${artId}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            // Не устанавливаем Content-Type для FormData
        },
        body: formData
        });
        
        console.log('[Art API] Update response status:', response.status);
        
        if (!response.ok) {
        let errorText = await response.text();
        console.error('[Art API] Update error response:', errorText);
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('[Art API] Update successful:', data);
        return formatArt(data);
    } catch (error) {
        console.error('[Art API] Error updating art:', error);
        throw error;
    }
    },
    async deleteArt(artId) {
    try {
        const token = getToken();
        
        if (!token) {
        throw new Error('Требуется авторизация для удаления арта');
        }
        
        console.log(`[API] Deleting art ${artId}`);
        
        const response = await fetch(`${API_URL}/arts/${artId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
        });
        
        console.log(`[API] Delete response status: ${response.status}`);
        
        if (response.status === 204) {
        console.log(`[API] Art ${artId} deleted successfully`);
        return true;
        }
        
        if (!response.ok) {
        const errorText = await response.text();
        console.error('[API] Delete error response:', errorText);
        throw new Error(`HTTP ${response.status}: ${response.statusText || 'Не удалось удалить арт'}`);
        }
        
        return true;
    } catch (error) {
        console.error('[API] Error deleting art:', error);
        throw error;
    }
    },

  // Экспортируем вспомогательные функции
  utils: {
    getImageUrl,
    getAlternativeImageUrl,
    handleImageError,
    preloadImage,
    formatArt,
    formatArtsArray,
    formatAuthor,
    formatTags,

    createFormData: (artData, imageFile) => {
    const formData = new FormData();
    
    // Обязательные поля
    if (artData.title) {
        formData.append('title', artData.title);
    }
    
    if (imageFile) {
        formData.append('imageFile', imageFile);
    }
    
    // Опциональные поля
    if (artData.description !== undefined) {
        formData.append('description', artData.description);
    }
    
    if (artData.isPublic !== undefined) {
        formData.append('isPublic', artData.isPublic.toString());
    }
    
    if (artData.projectDataUrl !== undefined) {
        formData.append('projectDataUrl', artData.projectDataUrl);
    }
    
    return formData;
    }
    
  }
};

export default artApi;