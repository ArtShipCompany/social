import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';

const requestProtected = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

const requestPublic = (url, options = {}) =>
  fetchWithErrorHandling(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

const formatTag = (tag) => {
  if (!tag) return null;
  return {
    id: tag.id,
    name: tag.name || tag.tagName,
    artCount: tag.artCount || 0,
    createdAt: tag.createdAt,
  };
};

// ВАЖНО: бэк возвращает плоский массив [TagDto], а не Page
const formatTagList = (data) => {
  if (!Array.isArray(data)) return [];
  return data.map(formatTag);
};

export const tagApi = {
  
  // === CRUD ТЕГОВ ===
  
  async createTag(name) {
    const data = await requestProtected(`${API_URL}/tags`, {
      method: 'POST',
      body: JSON.stringify({ name }), // TagCreateRequest
    });
    return formatTag(data);
  },
  
  async updateTag(id, name) {
    // Используем query param версию (проще)
    const data = await requestProtected(`${API_URL}/tags/${id}?name=${encodeURIComponent(name)}`, {
      method: 'PUT',
    });
    return formatTag(data);
  },
  
  // Альтернатива: обновление через JSON body
  async updateTagJson(id, name) {
    const data = await requestProtected(`${API_URL}/tags/${id}/update`, {
      method: 'PUT',
      body: JSON.stringify({ name }),
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
      body: JSON.stringify(tagNames), // List<String>
    });
    return Array.isArray(data) ? data.map(formatTag) : [];
  },
  
  // === ПУБЛИЧНЫЕ ОПЕРАЦИИ ===
  
  async getAllTags(page = 0, size = 50, sortBy = 'name', direction = 'asc') {
    // Бэк игнорирует пагинацию и возвращает плоский список!
    const url = `${API_URL}/tags?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
    const data = await requestPublic(url);
    return formatTagList(data); // НЕ formatTagPage, т.к. нет content/totalElements
  },
  
  async getTagById(id) {
    const data = await requestPublic(`${API_URL}/tags/${id}`);
    return formatTag(data);
  },
  
  async getTagByName(name) {
    const data = await requestPublic(`${API_URL}/tags/name/${encodeURIComponent(name)}`);
    return formatTag(data);
  },
  
  // searchTags не существует на бэке → используем autocomplete
  async searchTags(query, page = 0, size = 20) {
    return await this.autocompleteTags(query);
  },
  
  async getPopularTags(limit = 10) {
    const data = await requestPublic(`${API_URL}/tags/popular?limit=${limit}`);
    return formatTagList(data);
  },
  
  async autocompleteTags(query) {
    const data = await requestPublic(`${API_URL}/tags/autocomplete?q=${encodeURIComponent(query)}`);
    return formatTagList(data);
  },
  
  async tagExists(name) {
    try {
      const data = await requestPublic(`${API_URL}/tags/exists/${encodeURIComponent(name)}`);
      return data === true || data?.toString() === 'true';
    } catch {
      return false;
    }
  },
  
  // === СВЯЗИ АРТ-ТЕГ ===
  
  async addTagToArt(artId, tagId) {
    const data = await requestProtected(`${API_URL}/art-tags/art/${artId}/tag/${tagId}`, {
      method: 'POST',
    });
    return data; // возвращается ArtTagDto или error map
  },
  
  async removeTagFromArt(artId, tagId) {
    await requestProtected(`${API_URL}/art-tags/art/${artId}/tag/${tagId}`, {
      method: 'DELETE',
    });
    return true;
  },
  
  async checkTagArtRelation(artId, tagId) {
    try {
      const data = await requestProtected(`${API_URL}/art-tags/art/${artId}/tag/${tagId}/exists`);
      return data === true || data?.toString() === 'true';
    } catch {
      return false;
    }
  },
  
  // Получение тегов арта (используем endpoint из ArtTagController)
  async getTagsByArt(artId) {
    const data = await requestProtected(`${API_URL}/art-tags/art/${artId}/tags`);
    return formatTagList(data);
  },
  
  // Альтернативный эндпоинт (из TagController) - тоже рабочий
  async getTagsByArtAlt(artId) {
    const data = await requestPublic(`${API_URL}/tags/art/${artId}`);
    return formatTagList(data);
  },
  
  async addTagsBatchToArt(artId, tagNames) {
    await requestProtected(`${API_URL}/art-tags/art/${artId}/tags/batch`, {
      method: 'POST',
      body: JSON.stringify({ tagNames }), // AddTagsRequest
    });
    return true;
  },
  
  async removeAllTagsFromArt(artId) {
    await requestProtected(`${API_URL}/art-tags/art/${artId}/tags`, {
      method: 'DELETE',
    });
    return true;
  },
  
  async getTagCountByArt(artId) {
    const data = await requestProtected(`${API_URL}/art-tags/art/${artId}/tags/count`);
    return typeof data === 'number' ? data : data?.count ?? 0;
  },
  
  async getArtCountByTag(tagId) {
    // Используем endpoint из TagController (он проще)
    const data = await requestPublic(`${API_URL}/tags/${tagId}/art-count`);
    return typeof data === 'number' ? data : 0;
  },
  
  // === УТИЛИТЫ ===
  
  async getOrCreateTag(name) {
    try {
      return await this.getTagByName(name);
    } catch (error) {
      if (error?.status === 404 || error?.response?.status === 404) {
        return await this.createTag(name);
      }
      throw error;
    }
  },
  
  async processTagsString(artId, tagsString) {
    if (!tagsString?.trim()) return [];
    
    const tagNames = tagsString.split(/\s+/)
      .map(t => t.trim())
      .filter(Boolean)
      .map(t => t.startsWith('#') ? t.slice(1) : t)
      .filter(Boolean);
    
    if (tagNames.length === 0) return [];
    
    await this.addTagsBatchToArt(artId, tagNames);
    return tagNames;
  },
  
  formatTagsForDisplay(tagsArray) {
    if (!Array.isArray(tagsArray)) return '';
    return tagsArray.map(tag => {
      const name = typeof tag === 'string' ? tag : tag?.name || tag?.tagName || '';
      return name && !name.startsWith('#') ? `#${name}` : name;
    }).filter(Boolean).join(' ');
  },
  
  parseTagsString(tagsString) {
    if (!tagsString?.trim()) return [];
    return tagsString.split(/\s+/)
      .map(t => t.trim())
      .filter(Boolean)
      .map(t => t.startsWith('#') ? t.slice(1) : t);
  },
  
  async updateArtTags(artId, newTagsString) {
    await this.removeAllTagsFromArt(artId);
    if (newTagsString?.trim()) {
      await this.processTagsString(artId, newTagsString);
    }
    return true;
  },
  
  utils: {
    formatTag,
    formatTagList,
  }
};

export default tagApi;