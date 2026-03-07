// api/tagApi.js
const API_URL = 'http://localhost:8081/api';

const getToken = () => {
  return localStorage.getItem('accessToken');
};

async function fetchWithErrorHandling(url, options = {}) {
  const finalOptions = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };
  
  const token = getToken();
  if (token) {
    finalOptions.headers['Authorization'] = `Bearer ${token}`;
  }
  
  try {
    console.log(`[Tag API] ${options.method || 'GET'} ${url}`);
    const response = await fetch(url, finalOptions);
    
    if (response.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
      throw new Error('Сессия истекла. Пожалуйста, войдите снова.');
    }
    
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      try {
        const data = await response.json();
        errorMessage = data.error || data.message || errorMessage;
      } catch {
        // Если не удалось распарсить JSON
      }
      throw new Error(errorMessage);
    }
    
    if (response.status === 204) {
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('[Tag API] Error:', error);
    throw error;
  }
}

export const tagApi = {
  // === ОСНОВНЫЕ ОПЕРАЦИИ С ТЕГАМИ ===
  
  // Создать тег
  async createTag(name) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags`, {
        method: 'POST',
        body: JSON.stringify({ name })
      });
    } catch (error) {
      console.error('[Tag API] Error creating tag:', error);
      throw error;
    }
  },
  
  // Получить все теги
  async getAllTags(page = 0, size = 50, sortBy = 'name', direction = 'asc') {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/tags?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`
      );
    } catch (error) {
      console.error('[Tag API] Error getting all tags:', error);
      throw error;
    }
  },
  
  // Получить тег по ID
  async getTagById(id) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/${id}`);
    } catch (error) {
      console.error('[Tag API] Error getting tag by ID:', error);
      throw error;
    }
  },
  
  // Получить тег по имени
  async getTagByName(name) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/name/${name}`);
    } catch (error) {
      console.error('[Tag API] Error getting tag by name:', error);
      throw error;
    }
  },
  
  // Поиск тегов
  async searchTags(query, page = 0, size = 20) {
    try {
      return await fetchWithErrorHandling(
        `${API_URL}/tags/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`
      );
    } catch (error) {
      console.error('[Tag API] Error searching tags:', error);
      throw error;
    }
  },
  
  // Популярные теги
  async getPopularTags(limit = 10) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/popular?limit=${limit}`);
    } catch (error) {
      console.error('[Tag API] Error getting popular tags:', error);
      throw error;
    }
  },
  
  // Автодополнение тегов
  async autocompleteTags(query) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/autocomplete?q=${encodeURIComponent(query)}`);
    } catch (error) {
      console.error('[Tag API] Error autocomplete tags:', error);
      throw error;
    }
  },
  
  // Проверить существование тега
  async tagExists(name) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/exists/${name}`);
    } catch (error) {
      console.error('[Tag API] Error checking tag existence:', error);
      return false;
    }
  },
  
  // Обновить тег
  async updateTag(id, name) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/${id}?name=${encodeURIComponent(name)}`, {
        method: 'PUT'
      });
    } catch (error) {
      console.error('[Tag API] Error updating tag:', error);
      throw error;
    }
  },
  
  // Удалить тег
  async deleteTag(id) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/${id}`, {
        method: 'DELETE'
      });
    } catch (error) {
      console.error('[Tag API] Error deleting tag:', error);
      throw error;
    }
  },
  
  // Создать несколько тегов
  async createTagsBatch(tagNames) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/tags/batch`, {
        method: 'POST',
        body: JSON.stringify(tagNames)
      });
    } catch (error) {
      console.error('[Tag API] Error creating tags batch:', error);
      throw error;
    }
  },
  
  // === РАБОТА С АРТ-ТЕГ СВЯЗЯМИ ===
  
  // Добавить тег к арту
  async addTagToArt(artId, tagId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tag/${tagId}`, {
        method: 'POST'
      });
    } catch (error) {
      console.error('[Tag API] Error adding tag to art:', error);
      throw error;
    }
  },
  
  // Удалить тег из арта
  async removeTagFromArt(artId, tagId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tag/${tagId}`, {
        method: 'DELETE'
      });
    } catch (error) {
      console.error('[Tag API] Error removing tag from art:', error);
      throw error;
    }
  },
  
  // Проверить связь арт-тег
  async checkTagArtRelation(artId, tagId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tag/${tagId}/exists`);
    } catch (error) {
      console.error('[Tag API] Error checking tag-art relation:', error);
      return false;
    }
  },
  
  // Получить теги арта
  async getTagsByArt(artId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tags`);
    } catch (error) {
      console.error('[Tag API] Error getting tags by art:', error);
      throw error;
    }
  },
  
  // Получить арты по тегу
  async getArtsByTag(tagId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/tag/${tagId}/arts`);
    } catch (error) {
      console.error('[Tag API] Error getting arts by tag:', error);
      throw error;
    }
  },
  
  // Массовое добавление тегов к арту
  async addTagsBatchToArt(artId, tagNames) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tags/batch`, {
        method: 'POST',
        body: JSON.stringify({ tagNames })
      });
    } catch (error) {
      console.error('[Tag API] Error adding tags batch to art:', error);
      throw error;
    }
  },
  
  // Удалить все теги из арта
  async removeAllTagsFromArt(artId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tags`, {
        method: 'DELETE'
      });
    } catch (error) {
      console.error('[Tag API] Error removing all tags from art:', error);
      throw error;
    }
  },
  
  // Получить количество тегов у арта
  async getTagCountByArt(artId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/art/${artId}/tags/count`);
    } catch (error) {
      console.error('[Tag API] Error getting tag count by art:', error);
      return 0;
    }
  },
  
  // Получить количество артов по тегу
  async getArtCountByTag(tagId) {
    try {
      return await fetchWithErrorHandling(`${API_URL}/art-tags/tag/${tagId}/arts/count`);
    } catch (error) {
      console.error('[Tag API] Error getting art count by tag:', error);
      return 0;
    }
  },
  
  // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
  
  // Создать или получить существующий тег
  async getOrCreateTag(name) {
    try {
      // Сначала проверяем существование
      const exists = await this.tagExists(name);
      if (exists) {
        return await this.getTagByName(name);
      } else {
        return await this.createTag(name);
      }
    } catch (error) {
      console.error('[Tag API] Error getting or creating tag:', error);
      throw error;
    }
  },
  
  // Обработать строку тегов (например: "#живопись #art #fyp")
  async processTagsString(artId, tagsString) {
    try {
      if (!tagsString || !tagsString.trim()) {
        return [];
      }
      
      // Разделяем теги по пробелам и убираем #
      const tagNames = tagsString.split(' ')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0)
        .map(tag => tag.startsWith('#') ? tag.substring(1) : tag)
        .filter(tag => tag.length > 0);
      
      if (tagNames.length === 0) {
        return [];
      }
      
      // Создаем теги если их нет
      const createdTags = await this.createTagsBatch(tagNames);
      
      // Добавляем теги к арту
      const results = [];
      for (const tag of createdTags) {
        try {
          const result = await this.addTagToArt(artId, tag.id);
          results.push(result);
        } catch (error) {
          console.error(`[Tag API] Error adding tag ${tag.name} to art ${artId}:`, error);
        }
      }
      
      return results;
    } catch (error) {
      console.error('[Tag API] Error processing tags string:', error);
      throw error;
    }
  },
  
  // Форматировать теги для отображения
  formatTagsForDisplay(tagsArray) {
    if (!Array.isArray(tagsArray)) return '#no-tags';
    
    const tagStrings = tagsArray.map(tag => {
      if (typeof tag === 'string') {
        return tag.startsWith('#') ? tag : `#${tag}`;
      }
      if (tag && typeof tag === 'object') {
        const tagName = tag.name || tag.tag || tag.displayName || '';
        return tagName.startsWith('#') ? tagName : `#${tagName}`;
      }
      return '';
    }).filter(tag => tag !== '');
    
    return tagStrings.join(' ') || '#no-tags';
  },
  
  // Парсить строку тегов в массив имен
  parseTagsString(tagsString) {
    if (!tagsString || !tagsString.trim()) return [];
    
    return tagsString.split(' ')
      .map(tag => tag.trim())
      .filter(tag => tag.length > 0)
      .map(tag => tag.startsWith('#') ? tag.substring(1) : tag);
  },
  
  // Обновить теги арта (полная замена)
  async updateArtTags(artId, newTagsString) {
    try {
      // Удаляем все старые теги
      await this.removeAllTagsFromArt(artId);
      
      // Добавляем новые теги
      return await this.processTagsString(artId, newTagsString);
    } catch (error) {
      console.error('[Tag API] Error updating art tags:', error);
      throw error;
    }
  }
};

export default tagApi;