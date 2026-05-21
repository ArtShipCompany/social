// collectionsApi.js
import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';
const COLLECTIONS_BASE = `${API_URL}/collections`;
const COLLECTION_ARTS_BASE = `${API_URL}/collection-arts`;
const LIKED_ARTS_BASE = `${API_URL}/liked-arts`;

// === REQUEST HELPERS ===

// Стандартный JSON-запрос с авторизацией и авто-рефрешем токена
const requestJson = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

// Multipart запрос (для обложек коллекций) — Content-Type ставит браузер
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

// === FORMATTERS ===

// Форматирование CollectionDto с бэка
const formatCollection = (collection) => {
  if (!collection) return null;
  return {
    id: collection.id,
    title: collection.title || 'Без названия',
    description: collection.description || '',
    isPublic: collection.isPublic !== false,
    coverImageUrl: collectionsApi.getCoverImageUrl(collection),
    createdAt: collection.createdAt,
    userId: collection.userId,
    username: collection.username,
    artCount: collection.artCount || 0,
    // arts массив может быть большим, обычно не тянем его в списке
    arts: collection.arts?.map(art => ({
      id: art.id,
      title: art.title,
      imageUrl: art.imageUrl,
      author: art.author,
    })) || [],
  };
};

// Форматирование CollectionArtDto (связь коллекция-арт)
const formatCollectionArt = (collectionArt) => {
  if (!collectionArt) return null;
  return {
    collectionId: collectionArt.collectionId,
    collectionTitle: collectionArt.collectionTitle,
    artId: collectionArt.artId,
    artTitle: collectionArt.artTitle,
    artImage: collectionsApi.getArtImageUrl(collectionArt.artImage),
    savedAt: collectionArt.savedAt,
  };
};

// Форматирование Page<CollectionDto> с бэка
const formatCollectionPage = (pageData) => {
  if (!pageData) return { content: [], totalElements: 0, totalPages: 0, last: true };
  return {
    content: (pageData.content || []).map(formatCollection),
    totalElements: pageData.totalElements || 0,
    totalPages: pageData.totalPages || 0,
    number: pageData.number || 0,
    size: pageData.size || 20,
    last: pageData.last ?? true,
  };
};

// Форматирование Page<ArtDto> из collection-arts эндпоинтов
const formatArtPage = (pageData) => {
  if (!pageData) return { content: [], totalElements: 0, totalPages: 0, last: true };
  return {
    ...pageData,
    content: (pageData.content || []).map(art => ({
      ...art,
      imageUrl: collectionsApi.getArtImageUrl(art.imageUrl),
    })),
  };
};

// === VIRTUAL "LIKED" COLLECTION HELPERS ===

// ID виртуальной коллекции "Мне понравилось"
export const LIKED_COLLECTION_ID = '__liked__';

// Проверяем, является ли коллекция виртуальной "лайкнутой"
export const isLikedCollection = (collection) => 
  collection?.id === LIKED_COLLECTION_ID;

// Создаём объект виртуальной коллекции "Мне понравилось"
export const createLikedCollection = (artCount = 0, username = '') => ({
  id: LIKED_COLLECTION_ID,
  title: 'Мне понравилось',
  description: 'Автоматическая коллекция с вашими лайками',
  isPublic: false,
  coverImageUrl: '/icons/heart-collection.svg', // можно заменить на реальный путь
  createdAt: null,
  userId: null,
  username,
  artCount,
  isVirtual: true,
});

// === MAIN API EXPORT ===

export const collectionsApi = {
  
  // === COLLECTION CRUD ===
  
  // Создание коллекции (multipart: текст + файл обложки)
  async createCollection({ title, description, isPublic, coverImageFile }) {
    if (!title?.trim()) throw new Error('Название коллекции обязательно');
    
    const formData = new FormData();
    formData.append('title', title.trim());
    if (description !== undefined) formData.append('description', description);
    if (isPublic !== undefined) formData.append('isPublic', String(isPublic));
    if (coverImageFile instanceof File) formData.append('coverImageFile', coverImageFile);
    
    const data = await requestMultipart(COLLECTIONS_BASE, {
      method: 'POST',
      body: formData,
    });
    
    return formatCollection(data);
  },
  
  // Обновление коллекции (multipart)
  async updateCollection(collectionId, { title, description, isPublic, coverImageFile }) {
    const formData = new FormData();
    if (title !== undefined) formData.append('title', title);
    if (description !== undefined) formData.append('description', description);
    if (isPublic !== undefined) formData.append('isPublic', String(isPublic));
    if (coverImageFile instanceof File) formData.append('coverImageFile', coverImageFile);
    
    const data = await requestMultipart(`${COLLECTIONS_BASE}/${collectionId}`, {
      method: 'PUT',
      body: formData,
    });
    
    return formatCollection(data);
  },
  
  // Получение коллекции по ID (с проверкой прав доступа)
  async getCollection(collectionId, isAuthenticated = false) {
    // Обработка виртуальной коллекции "лайки"
    if (collectionId === LIKED_COLLECTION_ID) {
      if (!isAuthenticated) {
        throw new Error('Доступ к коллекции "Мне понравилось" требует авторизации');
      }
      // Реальные данные подгрузим отдельно через likedApi
      return createLikedCollection();
    }
    
    const url = `${COLLECTIONS_BASE}/${collectionId}`;
    const response = await fetch(url, {
      credentials: 'include',
      headers: isAuthenticated 
        ? { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        : {},
    });
    
    if (!response.ok) {
      if (response.status === 404) throw new Error('Коллекция не найдена');
      if (response.status === 403) throw new Error('Нет доступа к коллекции');
      throw new Error(`HTTP ${response.status}`);
    }
    
    const data = await response.json();
    return formatCollection(data);
  },
  
  // Удаление коллекции (не работает для виртуальной)
    async deleteCollection(collectionId) {
        const normalizedId = String(collectionId).toLowerCase();

        const blockedIds = [
            LIKED_COLLECTION_ID,
            '__liked__',
            'liked',
        ];

        if (blockedIds.includes(normalizedId)) {
            console.warn('🚫 Попытка удалить системную коллекцию');

            return {
                success: false,
                blocked: true,
                message: 'Системную коллекцию удалить нельзя',
            };
        }

        await requestJson(`${COLLECTIONS_BASE}/${collectionId}`, {
            method: 'DELETE'
        });

        return {
            success: true
        };
    },
  
  // === COLLECTION LISTS ===
  
  // Все публичные коллекции (пагинация)
  async getPublicCollections({ page = 0, size = 20, sortBy = 'createdAt', direction = 'desc' } = {}) {
    const url = `${COLLECTIONS_BASE}/public?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  // Коллекции конкретного пользователя (с учётом прав: все или только публичные)
  async getUserCollections(userId, { page = 0, size = 20, sortBy = 'createdAt', direction = 'desc', includeLiked = true } = {}) {
    const url = `${COLLECTIONS_BASE}/user/${userId}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    const pageData = formatCollectionPage(data);
    
    // Добавляем виртуальную коллекцию "лайки" в начало, если нужно
    if (includeLiked && userId === getCurrentUserId()) {
      const likedCount = await this.getLikedArtsCount();
      if (likedCount > 0) {
        const likedCollection = createLikedCollection(likedCount);
        console.log('✨ Adding liked collection:', likedCollection);
        pageData.content.unshift(likedCollection);
        pageData.totalElements += 1;
      }
    }
    
    return pageData;
  },
  
  // Только публичные коллекции пользователя
  async getUserPublicCollections(userId, { page = 0, size = 20 } = {}) {
    const url = `${COLLECTIONS_BASE}/user/${userId}/public?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  // Поиск публичных коллекций по запросу
  async searchCollections(query, { page = 0, size = 20 } = {}) {
    const url = `${COLLECTIONS_BASE}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  // === COLLECTION-ART RELATIONS ===
  
  // Добавить арт в коллекцию
  async addArtToCollection(collectionId, artId) {
    if (collectionId === LIKED_COLLECTION_ID) {
      // Для "лайков" используем отдельный эндпоинт
      return await likedApi.addLike(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/art/${artId}`;
    const data = await requestJson(url, { method: 'POST' });
    return formatCollectionArt(data);
  },
  
  // Удалить арт из коллекции
  async removeArtFromCollection(collectionId, artId) {
    if (collectionId === LIKED_COLLECTION_ID) {
      return await likedApi.removeLike(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/art/${artId}`;
    await requestJson(url, { method: 'DELETE' });
    return true;
  },
  
  // Проверить, есть ли арт в коллекции
  async isArtInCollection(collectionId, artId) {
    if (collectionId === LIKED_COLLECTION_ID) {
      return await likedApi.isLiked(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/art/${artId}/exists`;
    const data = await requestJson(url);
    return data === true;
  },
  
  // Получить арты коллекции (пагинация)
  async getArtsInCollection(collectionId, { page = 0, size = 20 } = {}) {
    if (collectionId === LIKED_COLLECTION_ID) {
      return await likedApi.getMyLikedArts({ page, size });
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/arts?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatArtPage(data);
  },
  
  // Получить количество артов в коллекции
  async getArtCountInCollection(collectionId) {
    if (collectionId === LIKED_COLLECTION_ID) {
      return await likedApi.getLikedCount();
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/arts/count`;
    const data = await requestJson(url);
    return data;
  },
  
  // Получить коллекции, в которых есть арт
  async getCollectionsContainingArt(artId, { page = 0, size = 20 } = {}) {
    const url = `${COLLECTION_ARTS_BASE}/art/${artId}/collections?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  // Переместить арт между коллекциями
  async moveArtBetweenCollections(artId, fromCollectionId, toCollectionId) {
    // Если участвует виртуальная коллекция — обрабатываем через лайки
    if (fromCollectionId === LIKED_COLLECTION_ID || toCollectionId === LIKED_COLLECTION_ID) {
      if (fromCollectionId === LIKED_COLLECTION_ID) {
        await likedApi.removeLike(getCurrentUserId(), artId);
      }
      if (toCollectionId === LIKED_COLLECTION_ID) {
        await likedApi.addLike(getCurrentUserId(), artId);
      }
      return true;
    }
    
    const url = `${COLLECTION_ARTS_BASE}/art/${artId}/move`;
    await requestJson(url, {
      method: 'POST',
      body: JSON.stringify({ fromCollectionId, toCollectionId }),
    });
    return true;
  },
  
  // Скопировать арт в коллекцию
  async copyArtToCollection(artId, collectionId) {
    if (collectionId === LIKED_COLLECTION_ID) {
      return await likedApi.addLike(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/art/${artId}/copy`;
    const data = await requestJson(url, {
      method: 'POST',
      body: JSON.stringify({ collectionId }),
    });
    return formatCollectionArt(data);
  },
  
  // === LIKED ARTS (для виртуальной коллекции) ===
  
  // Получить количество лайкнутых артов
  async getLikedArtsCount() {
    const url = `${LIKED_ARTS_BASE}/count`;
    const data = await requestJson(url);
    return data?.count || 0;
  },
  
  // Получить лайкнутые арты (пагинация)
  async getLikedArts({ page = 0, size = 20, sortBy = 'createdAt', direction = 'desc' } = {}) {
    const url = `${LIKED_ARTS_BASE}/me?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    return formatArtPage(data);
  },
  
  // === UTILS ===
  
  // Получение URL обложки коллекции с нормализацией путей
  getCoverImageUrl(collection) {
    if (!collection) return '/default-collection-cover.png';
    const coverUrl = collection.coverImageUrl;
    if (!coverUrl) return '/default-collection-cover.png';
    
    // Абсолютные URL возвращаем как есть
    if (coverUrl.startsWith('http://') || coverUrl.startsWith('https://')) {
      return coverUrl;
    }
    
    // Пути с бэка
    if (coverUrl.startsWith('/uploads/') || coverUrl.startsWith('/api/files/')) {
      return `http://localhost:8081${coverUrl}`;
    }
    
    // Если просто имя файла
    if (coverUrl.includes('.')) {
      return `http://localhost:8081/uploads/images/${coverUrl}`;
    }
    
    return '/default-collection-cover.png';
  },
  
  // Получение URL арта (используем тот же паттерн, что в artApi)
  getArtImageUrl(imageUrl) {
    if (!imageUrl) return '/default-art.jpg';
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
    }
    if (imageUrl.startsWith('/uploads/images/')) {
      return `http://localhost:8081${imageUrl}`;
    }
    if (imageUrl.startsWith('/api/files/images/')) {
      const filename = imageUrl.split('/').pop();
      return `http://localhost:8081/uploads/images/${filename}`;
    }
    if (imageUrl.startsWith('/')) {
      return `http://localhost:8081${imageUrl}`;
    }
    if (imageUrl.includes('.')) {
      return `http://localhost:8081/uploads/images/${imageUrl}`;
    }
    return '/default-art.jpg';
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
  
  // Хелпер для создания FormData при создании/обновлении коллекции
  createCollectionFormData({ title, description, isPublic, coverImageFile }) {
    const formData = new FormData();
    formData.append('title', title);
    if (description !== undefined) formData.append('description', description);
    if (isPublic !== undefined) formData.append('isPublic', String(isPublic));
    if (coverImageFile instanceof File) formData.append('coverImageFile', coverImageFile);
    return formData;
  },
  
  // Форматтеры и утилиты для экспорта
  formatCollection,
  formatCollectionArt,
  formatCollectionPage,
  
  utils: {
    formatCollection,
    formatCollectionArt,
    formatCollectionPage,
    getCoverImageUrl: (collection) => collectionsApi.getCoverImageUrl(collection),
    getArtImageUrl: (imageUrl) => collectionsApi.getArtImageUrl(imageUrl),
    getFullUrl: (path) => collectionsApi.getFullUrl(path),
    LIKED_COLLECTION_ID,
    isLikedCollection,
    createLikedCollection,
  }
};

// === INTERNAL HELPERS ===

// Получаем ID текущего пользователя из токена или localStorage
const getCurrentUserId = () => {
  try {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || payload.userId || payload.id;
  } catch {
    return null;
  }
};

import { likeApi as likedApi } from './likeApi';

export default collectionsApi;