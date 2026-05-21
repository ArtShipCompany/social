import { authApi, fetchWithErrorHandling } from './authApi';
import { likeApi as likedApi } from './likeApi';

const API_URL = 'http://localhost:8081/api';
const COLLECTIONS_BASE = `${API_URL}/collections`;
const COLLECTION_ARTS_BASE = `${API_URL}/collection-arts`;
const LIKED_ARTS_BASE = `${API_URL}/liked-arts`;

// === REQUEST HELPERS ===

const requestJson = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

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
    arts: collection.arts?.map(art => ({
      id: art.id,
      title: art.title,
      imageUrl: art.imageUrl,
      author: art.author,
    })) || [],
  };
};

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

export const LIKED_COLLECTION_ID = '__liked__';

export const isLikedCollection = (collection) => {
  if (!collection) return false;
  const id = String(collection.id).toLowerCase();
  return id === LIKED_COLLECTION_ID || id === 'liked';
};

export const createLikedCollection = (artCount = 0, username = '') => ({
  id: LIKED_COLLECTION_ID,
  title: 'Мне понравилось',
  description: 'Автоматическая коллекция с вашими лайками',
  isPublic: false,
  coverImageUrl: '/icons/heart-collection.svg',
  createdAt: null,
  userId: null,
  username,
  artCount,
  isVirtual: true,
});

// === MAIN API EXPORT ===

export const collectionsApi = {
  
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
  
  async getCollection(collectionId, isAuthenticated = false) {
    if (isLikedCollection({ id: collectionId })) {
      if (!isAuthenticated) {
        throw new Error('Доступ к коллекции "Мне понравилось" требует авторизации');
      }
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
  
  async deleteCollection(collectionId) {
    const normalizedId = String(collectionId).toLowerCase();
    const blockedIds = [LIKED_COLLECTION_ID, '__liked__', 'liked'];

    if (blockedIds.includes(normalizedId)) {
      console.warn('Попытка удалить системную коллекцию');
      return {
        success: false,
        blocked: true,
        message: 'Системную коллекцию удалить нельзя',
      };
    }

    await requestJson(`${COLLECTIONS_BASE}/${collectionId}`, {
      method: 'DELETE'
    });

    return { success: true };
  },
  
  async getPublicCollections({ page = 0, size = 20, sortBy = 'createdAt', direction = 'desc' } = {}) {
    const url = `${COLLECTIONS_BASE}/public?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  async getUserCollections(userId, { page = 0, size = 20, sortBy = 'createdAt', direction = 'desc', includeLiked = true } = {}) {
    const url = `${COLLECTIONS_BASE}/user/${userId}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    const pageData = formatCollectionPage(data);
    
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
  
  async getUserPublicCollections(userId, { page = 0, size = 20 } = {}) {
    const url = `${COLLECTIONS_BASE}/user/${userId}/public?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  async searchCollections(query, { page = 0, size = 20 } = {}) {
    const url = `${COLLECTIONS_BASE}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  async addArtToCollection(collectionId, artId) {
    if (isLikedCollection({ id: collectionId })) {
      return await likedApi.addLike(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/art/${artId}`;
    const data = await requestJson(url, { method: 'POST' });
    return formatCollectionArt(data);
  },
  
  async removeArtFromCollection(collectionId, artId) {
    if (isLikedCollection({ id: collectionId })) {
      return await likedApi.removeLike(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/art/${artId}`;
    await requestJson(url, { method: 'DELETE' });
    return true;
  },
  
  async isArtInCollection(collectionId, artId) {
    if (isLikedCollection({ id: collectionId })) {
      return await likedApi.isLiked(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/art/${artId}/exists`;
    const data = await requestJson(url);
    return data === true;
  },
  
  async getArtsInCollection(collectionId, { page = 0, size = 20 } = {}) {
    if (isLikedCollection({ id: collectionId })) {
      return await this.getLikedArts({ page, size });
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/arts?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatArtPage(data);
  },
  
  async getArtCountInCollection(collectionId) {
    if (isLikedCollection({ id: collectionId })) {
      return await this.getLikedArtsCount();
    }
    const url = `${COLLECTION_ARTS_BASE}/collection/${collectionId}/arts/count`;
    const data = await requestJson(url);
    return data;
  },
  
  async getCollectionsContainingArt(artId, { page = 0, size = 20 } = {}) {
    const url = `${COLLECTION_ARTS_BASE}/art/${artId}/collections?page=${page}&size=${size}`;
    const data = await requestJson(url);
    return formatCollectionPage(data);
  },
  
  async moveArtBetweenCollections(artId, fromCollectionId, toCollectionId) {
    if (isLikedCollection({ id: fromCollectionId }) || isLikedCollection({ id: toCollectionId })) {
      if (isLikedCollection({ id: fromCollectionId })) {
        await likedApi.removeLike(getCurrentUserId(), artId);
      }
      if (isLikedCollection({ id: toCollectionId })) {
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
  
  async copyArtToCollection(artId, collectionId) {
    if (isLikedCollection({ id: collectionId })) {
      return await likedApi.addLike(getCurrentUserId(), artId);
    }
    const url = `${COLLECTION_ARTS_BASE}/art/${artId}/copy`;
    const data = await requestJson(url, {
      method: 'POST',
      body: JSON.stringify({ collectionId }),
    });
    return formatCollectionArt(data);
  },
  
  async getLikedArtsCount() {
    const url = `${LIKED_ARTS_BASE}/count`;
    const data = await requestJson(url);
    return data?.count || 0;
  },
  
  async getLikedArts({ page = 0, size = 20, sortBy = 'createdAt', direction = 'desc' } = {}) {
    const url = `${LIKED_ARTS_BASE}/me?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestJson(url);
    return formatArtPage(data);
  },
  
  getCoverImageUrl(collection) {
    if (!collection) return '/default-collection-cover.png';
    const coverUrl = collection.coverImageUrl;
    if (!coverUrl) return '/default-collection-cover.png';
    
    if (coverUrl.startsWith('http://') || coverUrl.startsWith('https://')) {
      return coverUrl;
    }
    
    if (coverUrl.startsWith('/uploads/') || coverUrl.startsWith('/api/files/')) {
      return `http://localhost:8081${coverUrl}`;
    }
    
    if (coverUrl.includes('.')) {
      return `http://localhost:8081/uploads/images/${coverUrl}`;
    }
    
    return '/default-collection-cover.png';
  },
  
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
  
  createCollectionFormData({ title, description, isPublic, coverImageFile }) {
    const formData = new FormData();
    formData.append('title', title);
    if (description !== undefined) formData.append('description', description);
    if (isPublic !== undefined) formData.append('isPublic', String(isPublic));
    if (coverImageFile instanceof File) formData.append('coverImageFile', coverImageFile);
    return formData;
  },
  
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

export default collectionsApi;