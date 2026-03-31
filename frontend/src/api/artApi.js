// artApi.js
import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';

// === ХЕЛПЕРЫ ДЛЯ ЗАПРОСОВ ===

// Для JSON-запросов с авто-рефрешем (приватные эндпоинты)
const requestProtected = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

// Для публичных запросов (без рефреша, но с куками)
const requestPublic = (url, options = {}) =>
  fetchWithErrorHandling(url, {
    credentials: 'include',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

// Для multipart/form-data (файлы + авто-рефреш)
const requestMultipart = (url, options = {}) =>
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
  });

// === ФОРМАТТИРОВАНИЕ (твои хелперы, чуть почищенные) ===

const formatAuthor = (authorData, fallbackId, fallbackNickname, fallbackName) => {
  if (!authorData || typeof authorData !== 'object') {
    return {
      id: fallbackId || 'unknown',
      username: fallbackNickname || 'Неизвестный автор',
      displayName: fallbackName || 'Неизвестный автор',
      pfp: '/default-avatar.png'
    };
  }
  const avatarUrl = authorData.pfp || authorData.avatar || authorData.profilePicture || authorData.avatarUrl;
  return {
    id: authorData.id || authorData.userId || fallbackId || 'unknown',
    username: authorData.username,
    displayName: authorData.displayName,
    pfp: getFullUrl(avatarUrl)
  };
};

const formatTags = (tags) => {
  if (!tags) return '#no-tags';
  if (Array.isArray(tags)) {
    return tags.map(tag => {
      const name = typeof tag === 'string' ? tag : tag?.name || tag?.tag || tag?.id;
      if (!name) return '';
      const trimmed = String(name).trim();
      return trimmed.startsWith('#') ? trimmed : `#${trimmed}`;
    }).filter(Boolean).join(' ') || '#no-tags';
  }
  if (typeof tags === 'string') {
    const trimmed = tags.trim();
    return trimmed.startsWith('#') ? trimmed : `#${trimmed}`;
  }
  return '#no-tags';
};

const getImageUrl = (imageUrl) => {
  if (!imageUrl) return '/default-art.jpg';
  if (imageUrl.startsWith('http')) return imageUrl;
  if (imageUrl.startsWith('/api/files/images/')) {
    return `/uploads/images/${imageUrl.split('/').pop()}`;
  }
  if (imageUrl.startsWith('/uploads/images/')) return imageUrl;
  if (imageUrl.startsWith('/')) return imageUrl;
  if (imageUrl.includes('.')) return `/uploads/images/${imageUrl}`;
  return '/default-art.jpg';
};

export const getFullUrl = (path) => {
  if (!path) return '/default-avatar.png';
  if (path.startsWith('http')) return path;
  let finalPath = path;
  if (path.startsWith('/api/files/images/')) {
    finalPath = `/uploads/images/${path.split('/').pop()}`;
  } else if (path.startsWith('uploads/')) {
    finalPath = `/${path}`;
  } else if (!path.includes('/')) {
    finalPath = `/uploads/images/${path}`;
  }
  return `http://localhost:8081${finalPath}`;
};

const getAlternativeImageUrl = (imageUrl) => {
  if (!imageUrl) return null;
  if (imageUrl.startsWith('/uploads/images/')) {
    return imageUrl.replace('/uploads/images/', '/api/files/images/');
  }
  if (imageUrl.startsWith('/api/files/images/')) {
    return imageUrl.replace('/api/files/images/', '/uploads/images/');
  }
  return null;
};

export const handleImageError = (event, currentImageUrl) => {
  console.error('Image load error for URL:', currentImageUrl);
  const alternativeUrl = getAlternativeImageUrl(currentImageUrl);
  if (alternativeUrl) {
    event.target.src = alternativeUrl;
    return true;
  }
  event.target.src = '/default-art.jpg';
  return false;
};

export const preloadImage = (imageUrl) => {
  return new Promise((resolve, reject) => {
    if (!imageUrl || imageUrl === '/default-art.jpg') {
      resolve('/default-art.jpg');
      return;
    }
    const img = new Image();
    img.src = imageUrl;
    img.onload = () => resolve(imageUrl);
    img.onerror = () => {
      const alt = getAlternativeImageUrl(imageUrl);
      if (alt) {
        const altImg = new Image();
        altImg.src = alt;
        altImg.onload = () => resolve(alt);
        altImg.onerror = () => reject(new Error('Both URLs failed'));
      } else {
        reject(new Error('Image load failed'));
      }
    };
  });
};

const formatArt = (art) => {
  if (!art) return null;
  return {
    ...art,
    imageUrl: getImageUrl(art.imageUrl || art.image || art.imagePath || ''),
    tags: formatTags(art.tags),
    author: formatAuthor(art.author, art.authorId, art.authorName),
    createdAt: art.createdAt,
  };
};

const formatPage = (pageData) => {
  if (!pageData?.content) return pageData;
  return {
    ...pageData,
    content: pageData.content.map(art => formatArt(art)),
  };
};

// === API МЕТОДЫ ===

export const artApi = {
  
  // PUBLIC ENDPOINTS
  
  async getPublicArts(page = 0, size = 30, sortBy = 'createdAt', direction = 'desc') {
    const url = `${API_URL}/arts/public?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestPublic(url);
    return formatPage(data);
  },

  async getArtById(artId) {
    const data = await requestPublic(`${API_URL}/arts/${artId}`);
    return formatArt(data);
  },

  async searchByTag(tagName, page = 0, size = 30) {
    const clean = tagName.replace(/^#/, '');
    const data = await requestPublic(`${API_URL}/arts/tag/${clean}?page=${page}&size=${size}`);
    return formatPage(data);
  },

  async searchByTitle(title, page = 0, size = 30) {
    const data = await requestPublic(
      `${API_URL}/arts/search?title=${encodeURIComponent(title)}&page=${page}&size=${size}`
    );
    return formatPage(data);
  },

  // PRIVATE ENDPOINTS
  
  async getFeedArts(page = 0, size = 30) {
    const data = await requestProtected(
      `${API_URL}/arts/feed?page=${page}&size=${size}`
    );
    return formatPage(data);
  },

  async getArtsByAuthor(userId, page = 0, size = 30) {
    const data = await requestProtected(
      `${API_URL}/arts/author/${userId}?page=${page}&size=${size}`
    );
    return formatPage(data);
  },

  async getMyArts(page = 0, size = 20) {
    const data = await requestProtected(
      `${API_URL}/arts/my-arts?page=${page}&size=${size}`
    );
    return formatPage(data);
  },

  async checkArtAccess(artId) {
    return requestProtected(`${API_URL}/arts/${artId}/access`);
  },

  
  async createArt(artData, imageFile) {
    if (!artData?.title?.trim()) throw new Error('Заголовок обязателен');
    if (!imageFile) throw new Error('Изображение обязательно');
    if (!imageFile.type?.startsWith('image/')) throw new Error('Файл должен быть изображением');
    if (imageFile.size > 10 * 1024 * 1024) throw new Error('Файл больше 10MB');

    const formData = new FormData();
    formData.append('title', artData.title.trim());
    if (artData.description) formData.append('description', artData.description);
    if (artData.isPublicFlag !== undefined) {
      formData.append('isPublicFlag', String(artData.isPublicFlag));
    }
    if (artData.projectDataUrl) formData.append('projectDataUrl', artData.projectDataUrl);
    formData.append('imageFile', imageFile);

    const data = await requestMultipart(`${API_URL}/arts`, {
      method: 'POST',
      body: formData,
    });
    return formatArt(data);
  },

  async updateArt(artId, artData, imageFile = null) {
    const formData = new FormData();
    if (artData?.title !== undefined) formData.append('title', artData.title);
    if (artData?.description !== undefined) formData.append('description', artData.description);
    if (imageFile) formData.append('imageFile', imageFile);

    const data = await requestMultipart(`${API_URL}/arts/${artId}`, {
      method: 'PUT',
      body: formData,
    });
    return formatArt(data);
  },

  async updateArtPrivacy(artId, isPublicFlag, userId) {
    const formData = new FormData();
    formData.append('isPublicFlag', String(isPublicFlag));
    
    const data = await requestMultipart(
      `${API_URL}/arts/${artId}/privacy?userId=${userId}`,
      {
        method: 'PATCH',
        body: formData,
      }
    );
    return formatArt(data);
  },

  async deleteArt(artId) {
    await requestProtected(`${API_URL}/arts/${artId}`, { method: 'DELETE' });
    return true;
  },

  utils: {
    getImageUrl,
    getAlternativeImageUrl,
    handleImageError,
    preloadImage,
    formatArt,
    formatPage,
    formatAuthor,
    formatTags,
    getFullUrl,
    createFormData: (artData, imageFile) => {
      const formData = new FormData();
      if (artData.title) formData.append('title', artData.title);
      if (imageFile) formData.append('imageFile', imageFile);
      if (artData.description !== undefined) formData.append('description', artData.description);
      if (artData.isPublicFlag !== undefined) formData.append('isPublicFlag', String(artData.isPublicFlag));
      if (artData.projectDataUrl !== undefined) formData.append('projectDataUrl', artData.projectDataUrl);
      return formData;
    }
  }
};

export default artApi;