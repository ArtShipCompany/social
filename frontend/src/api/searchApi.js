import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';
const BASE_PATH = `${API_URL}/search`;

// Запросы с авторизацией (для приватных эндпоинтов)
const requestProtected = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

// Публичные запросы
const requestPublic = (url, options = {}) =>
  fetchWithErrorHandling(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

const formatArt = (art) => {
  if (!art) return null;
  return {
    ...art,
    type: 'art',
    imageUrl: art.imageUrl || art.image || '/default-art.jpg',
    tags: Array.isArray(art.tags) ? art.tags.join(' ') : art.tags || '',
    author: art.author || { id: art.authorId, username: art.authorName },
  };
};

const formatUser = (user) => {
  if (!user) return null;
  return {
    ...user,
    type: 'user',
    displayName: user.displayName || user.username,
    avatarUrl: user.avatarUrl || '/default-avatar.png',
  };
};

// Форматирование страницы результатов
const formatSearchPage = (pageData, type = 'art') => {
  if (!pageData?.content) return { content: [], totalElements: 0, last: true };
  
  const formatter = type === 'user' ? formatUser : formatArt;
  return {
    ...pageData,
    content: pageData.content.map(item => formatter(item)),
  };
};

export const searchApi = {
  
  // УНИВЕРСАЛЬНЫЙ ПОИСК (арты + юзеры)
  async searchAll(query, params = {}) {
    const {
      artsPage = 0, artsSize = 10, artsSort = 'createdAt,desc',
      usersPage = 0, usersSize = 10, usersSort = 'createdAt,desc'
    } = params;
    
    const url = `${BASE_PATH}/all?query=${encodeURIComponent(query)}&artsPage=${artsPage}&artsSize=${artsSize}&usersPage=${usersPage}&usersSize=${usersSize}&artsSort=${artsSort}&usersSort=${usersSort}`;
    const data = await requestPublic(url);
    
    return {
      arts: formatSearchPage(data?.arts, 'art'),
      users: formatSearchPage(data?.users, 'user'),
      totalArts: data?.totalArts || 0,
      totalUsers: data?.totalUsers || 0,
    };
  },

  // БЫСТРЫЙ ПОИСК (для автокомплита)
  async quickSearch(query) {
    const data = await requestPublic(`${BASE_PATH}/quick?query=${encodeURIComponent(query)}`);
    return {
      arts: data?.arts?.content?.map(formatArt) || [],
      users: data?.users?.content?.map(formatUser) || [],
    };
  },

  // СЧЁТЧИКИ результатов
  async getSearchCounts(query) {
    const data = await requestPublic(`${BASE_PATH}/counts?query=${encodeURIComponent(query)}`);
    return {
      arts: data?.arts || 0,
      users: data?.users || 0,
    };
  },

  // ПОИСК ПО ТЕГАМ (арты)
  
  // Один тег
  async getByTag(tagName, page = 0, size = 20, sort = 'createdAt,desc') {
    const clean = tagName.replace(/^#/, '');
    const data = await requestPublic(`${BASE_PATH}/tag/${encodeURIComponent(clean)}?page=${page}&size=${size}&sort=${sort}`);
    return formatSearchPage(data, 'art');
  },

  // Несколько тегов: AND (все теги должны быть)
  async getByTagsAnd(tags, page = 0, size = 20, sort = 'createdAt,desc') {
    const tagsStr = Array.isArray(tags) ? tags.join(',') : tags;
    const data = await requestPublic(`${BASE_PATH}/tags/and?tags=${encodeURIComponent(tagsStr)}&page=${page}&size=${size}&sort=${sort}`);
    return formatSearchPage(data, 'art');
  },

  // Несколько тегов: OR (любой из тегов)
  async getByTagsOr(tags, page = 0, size = 20, sort = 'createdAt,desc') {
    const tagsStr = Array.isArray(tags) ? tags.join(',') : tags;
    const data = await requestPublic(`${BASE_PATH}/tags/or?tags=${encodeURIComponent(tagsStr)}&page=${page}&size=${size}&sort=${sort}`);
    return formatSearchPage(data, 'art');
  },

  // Гибкий поиск по тегам с выбором режима
  async getByTags(tags, { mode = 'and', page = 0, size = 20, sort = 'createdAt,desc' } = {}) {
    const tagsStr = Array.isArray(tags) ? tags.join(',') : tags;
    const data = await requestPublic(`${BASE_PATH}/tags?tags=${encodeURIComponent(tagsStr)}&mode=${mode}&page=${page}&size=${size}&sort=${sort}`);
    return formatSearchPage(data, 'art');
  },

  // ПОИСК ПОЛЬЗОВАТЕЛЕЙ (отдельно, если нужно)
  async searchUsers(query, page = 0, size = 20, sort = 'createdAt,desc') {
    // Используем универсальный поиск, но берём только users
    const result = await this.searchAll(query, { usersPage: page, usersSize: size, usersSort: sort, artsSize: 0 });
    return result.users;
  },

  // ПОИСК АРТОВ (отдельно, если нужно)
  async searchArts(query, page = 0, size = 20, sort = 'createdAt,desc') {
    const result = await this.searchAll(query, { artsPage: page, artsSize: size, artsSort: sort, usersSize: 0 });
    return result.arts;
  },

  utils: {
    formatArt,
    formatUser,
    formatSearchPage,
  }
};

export default searchApi;