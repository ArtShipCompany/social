// api/tagApi.js
import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';

// === ХЕЛПЕРЫ ДЛЯ ЗАПРОСОВ ===

// Для приватных запросов с авто-рефрешем
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

// === ФОРМАТТИРОВАНИЕ ===

const formatTag = (tag) => {
  if (!tag) return null;
  return {
    id: tag.id,
    name: tag.name || tag.tagName,
    artCount: tag.artCount || 0,
    createdAt: tag.createdAt,
  };
};

const formatTagPage = (pageData) => {
  if (!pageData?.content) return pageData;
  return {
    ...pageData,
    content: pageData.content.map(formatTag),
  };
};

// === API МЕТОДЫ ===

export const tagApi = {
  
  // 🔐 ПРИВАТНЫЕ ОПЕРАЦИИ (с авто-рефрешем)
  
  async createTag(name) {
    const data = await requestProtected(`${API_URL}/tags`, {
      method: 'POST',
      body: JSON.stringify({ name }),
    });
    return formatTag(data);
  },
  
  async updateTag(id, name) {
    const data = await requestProtected(`${API_URL}/tags/${id}?name=${encodeURIComponent(name)}`, {
      method: 'PUT',
    });
    return formatTag(data);
  },
  
  async deleteTag(id) {
    await requestProtected(`${API_URL}/tags/${id}`, { method: 'DELETE' });
    return true;
  },
  
  async createTagsBatch(tagNames) {
    const data = await requestProtected(`${API_URL}/tags/batch`, {
      method: 'POST',
      body: JSON.stringify(tagNames),
    });
    return Array.isArray(data) ? data.map(formatTag) : [];
  },
  
  // 🌐 ПУБЛИЧНЫЕ ОПЕРАЦИИ
  
  async getAllTags(page = 0, size = 50, sortBy = 'name', direction = 'asc') {
    const url = `${API_URL}/tags?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestPublic(url);
    return formatTagPage(data);
  },
  
  async getTagById(id) {
    const data = await requestPublic(`${API_URL}/tags/${id}`);
    return formatTag(data);
  },
  
  async getTagByName(name) {
    const data = await requestPublic(`${API_URL}/tags/name/${name}`);
    return formatTag(data);
  },
  
  async searchTags(query, page = 0, size = 20) {
    const url = `${API_URL}/tags/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    const data = await requestPublic(url);
    return formatTagPage(data);
  },
  
  async getPopularTags(limit = 10) {
    const data = await requestPublic(`${API_URL}/tags/popular?limit=${limit}`);
    return Array.isArray(data) ? data.map(formatTag) : [];
  },
  
  async autocompleteTags(query) {
    const data = await requestPublic(`${API_URL}/tags/autocomplete?q=${encodeURIComponent(query)}`);
    return Array.isArray(data) ? data.map(formatTag) : [];
  },
  
  async tagExists(name) {
    try {
      await requestPublic(`${API_URL}/tags/exists/${name}`);
      return true;
    } catch {
      return false;
    }
  },
  
  // 🔗 СВЯЗИ АРТ-ТЕГ (приватные)
  
  async addTagToArt(artId, tagId) {
    return requestProtected(`${API_URL}/art-tags/art/${artId}/tag/${tagId}`, {
      method: 'POST',
    });
  },
  
  async removeTagFromArt(artId, tagId) {
    await requestProtected(`${API_URL}/art-tags/art/${artId}/tag/${tagId}`, {
      method: 'DELETE',
    });
    return true;
  },
  
  async checkTagArtRelation(artId, tagId) {
    try {
      await requestProtected(`${API_URL}/art-tags/art/${artId}/tag/${tagId}/exists`);
      return true;
    } catch {
      return false;
    }
  },
  
  async getTagsByArt(artId) {
    const data = await requestProtected(`${API_URL}/art-tags/art/${artId}/tags`);
    return Array.isArray(data) ? data.map(formatTag) : [];
  },
  
  async addTagsBatchToArt(artId, tagNames) {
    return requestProtected(`${API_URL}/art-tags/art/${artId}/tags/batch`, {
      method: 'POST',
      body: JSON.stringify({ tagNames }),
    });
  },
  
  async removeAllTagsFromArt(artId) {
    await requestProtected(`${API_URL}/art-tags/art/${artId}/tags`, {
      method: 'DELETE',
    });
    return true;
  },
  
  async getTagCountByArt(artId) {
    const data = await requestProtected(`${API_URL}/art-tags/art/${artId}/tags/count`);
    return data?.count ?? 0;
  },
  
  async getArtCountByTag(tagId) {
    const data = await requestProtected(`${API_URL}/art-tags/tag/${tagId}/arts/count`);
    return data?.count ?? 0;
  },
  
  // 🎯 УТИЛИТЫ
  
  async getOrCreateTag(name) {
    const exists = await this.tagExists(name);
    if (exists) {
      return await this.getTagByName(name);
    }
    return await this.createTag(name);
  },
  
  async processTagsString(artId, tagsString) {
    if (!tagsString?.trim()) return [];
    
    const tagNames = tagsString.split(' ')
      .map(t => t.trim())
      .filter(Boolean)
      .map(t => t.startsWith('#') ? t.slice(1) : t)
      .filter(Boolean);
    
    if (tagNames.length === 0) return [];
    
    const results = [];
    for (const tagName of tagNames) {
      try {
        const tag = await this.getOrCreateTag(tagName);
        await this.addTagToArt(artId, tag.id);
        results.push(tag);
      } catch (err) {
        console.error(`[Tag API] Ошибка с тегом "${tagName}":`, err);
      }
    }
    return results;
  },
  
  formatTagsForDisplay(tagsArray) {
    if (!Array.isArray(tagsArray)) return '#no-tags';
    return tagsArray.map(tag => {
      const name = typeof tag === 'string' ? tag : tag?.name || tag?.tagName || '';
      return name.startsWith('#') ? name : `#${name}`;
    }).filter(Boolean).join(' ') || '#no-tags';
  },
  
  parseTagsString(tagsString) {
    if (!tagsString?.trim()) return [];
    return tagsString.split(' ')
      .map(t => t.trim())
      .filter(Boolean)
      .map(t => t.startsWith('#') ? t.slice(1) : t);
  },
  
  async updateArtTags(artId, newTagsString) {
    await this.removeAllTagsFromArt(artId);
    return await this.processTagsString(artId, newTagsString);
  },
  
  // Экспорт форматтеров
  utils: {
    formatTag,
    formatTagPage,
  }
};

export default tagApi;