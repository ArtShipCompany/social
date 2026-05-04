import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';
const SEARCH_BASE = `${API_URL}/search`;

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

// Форматирование арта (адаптировано под ArtDto с бэка)
const formatArt = (art) => {
  if (!art) return null;
  return {
    ...art,
    type: 'art',
    imageUrl: art.imageUrl || art.image || art.imagePath || '/default-art.jpg',
    tags: Array.isArray(art.tags) 
      ? art.tags.map(t => t?.name || t?.tagName || t).filter(Boolean).join(' ') 
      : art.tags || '',
    author: art.author || (art.authorId ? { id: art.authorId, username: art.authorName } : null),
  };
};

const formatUser = (user) => {
  if (!user) return null;
  return {
    ...user,
    type: 'user',
    displayName: user.displayName || user.username || user.login,
    avatarUrl: user.avatarUrl || user.avatar || '/default-avatar.png',
  };
};

// Адаптация SearchResult с бэка под фронт
const adaptSearchResult = (result, type = 'all') => {
  if (!result) return { arts: [], users: [], totalArts: 0, totalUsers: 0 };
  
  // Объединяем арты по заголовку и тегам, если нужен общий список
  const allArts = [
    ...(result.artsByTitle || []),
    ...(result.artsByTags || [])
  ].filter((v, i, a) => a.findIndex(x => x.id === v.id) === i); // unique by id
  
  return {
    arts: allArts.map(formatArt),
    users: (result.usersByUsername || []).map(formatUser),
    totalArts: (result.totalArtsByTitle || 0) + (result.totalArtsByTags || 0),
    totalUsers: result.totalUsers || 0,
    // сохраняем исходные поля для продвинутой логики
    _raw: result,
  };
};

export const searchApi = {
  
  // УМНЫЙ ПОИСК (основной эндпоинт)
  async smartSearch(query, limit = 20) {
    const url = `${SEARCH_BASE}/smart?query=${encodeURIComponent(query)}&limit=${limit}`;
    const data = await requestPublic(url);
    return adaptSearchResult(data);
  },

  // УМНЫЙ ПОИСК С ПАГИНАЦИЕЙ
  async smartSearchPaginated(query, params = {}) {
    const {
      artsTitlePage = 0,
      artsTagsPage = 0, 
      usersPage = 0,
      size = 20
    } = params;
    
    const url = `${SEARCH_BASE}/smart/paginated?query=${encodeURIComponent(query)}&artsTitlePage=${artsTitlePage}&artsTagsPage=${artsTagsPage}&usersPage=${usersPage}&size=${size}`;
    const data = await requestPublic(url);
    return adaptSearchResult(data);
  },

  // ПОИСК ТОЛЬКО АРТОВ (по заголовку + тегам)
  async searchArts(query, page = 0, size = 20) {
    const result = await this.smartSearchPaginated(query, {
      artsTitlePage: page,
      artsTagsPage: page,
      usersPage: 0,
      size
    });
    return {
      content: result.arts,
      totalElements: result.totalArts,
      last: result.arts.length < size,
    };
  },

  // ПОИСК ТОЛЬКО ПОЛЬЗОВАТЕЛЕЙ
  async searchUsers(query, page = 0, size = 20) {
    const result = await this.smartSearchPaginated(query, {
      artsTitlePage: 0,
      artsTagsPage: 0,
      usersPage: page,
      size
    });
    return {
      content: result.users,
      totalElements: result.totalUsers,
      last: result.users.length < size,
    };
  },

  // БЫСТРЫЙ ПОИСК (для автокомплита) - через smart с маленьким лимитом
  async quickSearch(query) {
    return await this.smartSearch(query, 10);
  },

  // СЧЁТЧИКИ - эмулируем через smart с limit=0
  async getSearchCounts(query) {
    const data = await requestPublic(`${SEARCH_BASE}/smart?query=${encodeURIComponent(query)}&limit=0`);
    return {
      arts: (data?.totalArtsByTitle || 0) + (data?.totalArtsByTags || 0),
      users: data?.totalUsers || 0,
    };
  },

  // ПОИСК ПО ТЕГАМ (арты) - через smart с #префиксом
  async getByTag(tagName, page = 0, size = 20) {
    const clean = tagName.replace(/^#/, '');
    // Бэк понимает #теги в smartSearch
    const result = await this.smartSearchPaginated(`#${clean}`, {
      artsTitlePage: 0,
      artsTagsPage: page,
      usersPage: 0,
      size
    });
    return {
      content: result.arts,
      totalElements: result.totalArts,
      last: result.arts.length < size,
    };
  },

  // Поиск по нескольким тегам (на фронте, т.к. бэк не поддерживает AND/OR из коробки)
  async getByTags(tags, { mode = 'and', page = 0, size = 20 } = {}) {
    // Эмуляция на фронте: делаем запрос по каждому тегу и объединяем
    const results = await Promise.all(
      tags.map(tag => this.getByTag(tag, page, size))
    );
    
    if (mode === 'or') {
      // OR: объединяем все результаты
      const allArts = results.flatMap(r => r.content);
      const unique = allArts.filter((v, i, a) => a.findIndex(x => x.id === v.id) === i);
      return {
        content: unique.slice(0, size),
        totalElements: unique.length,
        last: unique.length < size,
      };
    } else {
      // AND: оставляем только арты, которые есть во всех результатах
      const [first, ...rest] = results;
      let filtered = first.content;
      for (const r of rest) {
        const ids = new Set(r.content.map(a => a.id));
        filtered = filtered.filter(a => ids.has(a.id));
      }
      return {
        content: filtered.slice(0, size),
        totalElements: filtered.length,
        last: filtered.length < size,
      };
    }
  },

  utils: {
    formatArt,
    formatUser,
    adaptSearchResult,
  }
};

export default searchApi;