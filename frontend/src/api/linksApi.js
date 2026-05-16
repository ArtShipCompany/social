import { authApi, fetchWithErrorHandling } from './authApi';

const API_URL = 'http://localhost:8081/api';
const LINKS_BASE = `${API_URL}/social-links`;

// JSON-запрос с авторизацией
const requestJson = (url, options = {}) => 
  authApi.fetchProtected(url, {
    credentials: 'include',
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });

// Форматирование соц-ссылки с бэка
const formatLink = (link) => {
  if (!link) return null;
  return {
    id: link.id,
    platform: link.platform, // enum: 'TELEGRAM', 'VKONTAKTE', etc.
    platformName: link.platform?.displayName || link.platform, // для отображения
    url: link.url,
    visible: link.visible !== false,
    displayOrder: link.displayOrder ?? 0,
    createdAt: link.createdAt,
    updatedAt: link.updatedAt,
  };
};

// Форматирование списка ссылок
const formatLinksList = (data) => {
  if (!data?.socialLinks) return [];
  return data.socialLinks.map(formatLink);
};

// Получение иконки платформы (можно расширить)
const getPlatformIcon = (platform) => {
  const icons = {
    TELEGRAM: 'TG',
    VKONTAKTE: 'VK',
    YOUTUBE: 'YT',
    TWITTER: 'X',
    TIKTOK: 'TT',
  };
  return icons[platform] || 'link';
};

// Получение базового URL платформы
const getPlatformBaseUrl = (platform) => {
  const urls = {
    TELEGRAM: 'https://t.me/',
    VKONTAKTE: 'https://vk.com/',
    YOUTUBE: 'https://youtube.com/',
    TWITTER: 'https://twitter.com/',
    TIKTOK: 'https://tiktok.com/@',
  };
  return urls[platform] || '';
};

// Форматирование полного URL (если передан только юзернейм)
const formatFullUrl = (platform, input) => {
  if (!input) return '';
  if (input.startsWith('http://') || input.startsWith('https://')) return input;
  
  const baseUrl = getPlatformBaseUrl(platform);
  if (baseUrl) {
    // Убираем @ если есть в начале юзернейма
    const username = input.replace(/^@/, '');
    return `${baseUrl}${username}`;
  }
  return input;
};

const extractUsername = (platform, fullUrl) => {
    if (!fullUrl) return '';
    const baseUrl = getPlatformBaseUrl(platform);
    if (baseUrl && fullUrl.startsWith(baseUrl)) {
      return fullUrl.replace(baseUrl, '').replace(/^@/, '');
    }
    return fullUrl;
};

export const linksApi = {
  
  // === GET LINKS ===
  
  // Мои ссылки (текущий пользователь)
  async getMyLinks(onlyVisible = true) {
    const url = `${LINKS_BASE}/me?onlyVisible=${onlyVisible}`;
    const data = await requestJson(url);
    return {
      links: formatLinksList(data),
      count: data?.count || 0,
    };
  },
  
  // Ссылки другого пользователя
  async getUserLinks(userId, onlyVisible = true) {
    const url = `${LINKS_BASE}/user/${userId}?onlyVisible=${onlyVisible}`;
    const data = await requestJson(url);
    return {
      userId: data?.userId,
      links: formatLinksList(data),
      count: data?.count || 0,
    };
  },
  
  // === CRUD ===
  
  // Добавить ссылку
  async addLink(platform, url, { visible = true, displayOrder = 0 } = {}) {
    const data = await requestJson(`${LINKS_BASE}`, {
      method: 'POST',
      body: JSON.stringify({
        platform,
        url,
        visible,
        displayOrder,
      }),
    });
    return formatLink(data);
  },
  
  // Обновить ссылку
  async updateLink(linkId, platform, url, { visible = true, displayOrder = 0 } = {}) {
    const data = await requestJson(`${LINKS_BASE}/${linkId}`, {
      method: 'PUT',
      body: JSON.stringify({
        platform,
        url,
        visible,
        displayOrder,
      }),
    });
    return formatLink(data);
  },
  
  // Удалить ссылку
  async deleteLink(linkId) {
    await requestJson(`${LINKS_BASE}/${linkId}`, { method: 'DELETE' });
    return true;
  },
  
  // === BATCH & ORDER ===
  
  // Обновить порядок отображения
  async updateOrder(linkIds) {
    // linkIds: массив ID в новом порядке [1, 3, 2, ...]
    await requestJson(`${LINKS_BASE}/order`, {
      method: 'PUT',
      body: JSON.stringify(linkIds),
    });
    return true;
  },
  
  // Массовое обновление всех ссылок (полная замена)
  async updateAllLinks(linksArray) {
    // linksArray: массив объектов { platform, url, visible, displayOrder }
    const requests = linksArray.map(link => ({
      platform: link.platform,
      url: link.url,
      visible: link.visible !== false,
      displayOrder: link.displayOrder ?? 0,
    }));
    
    const data = await requestJson(`${LINKS_BASE}/batch`, {
      method: 'PUT',
      body: JSON.stringify(requests),
    });
    
    return {
      links: formatLinksList(data),
      message: data?.message,
    };
  },
  
  // === UTILS ===
  
  // Получить список доступных платформ
  async getPlatforms() {
    const data = await requestJson(`${LINKS_BASE}/platforms`);
    return data?.platforms || [];
  },
  
  // Валидация URL для платформы
  async validateUrl(url, platform) {
    const data = await requestJson(
      `${LINKS_BASE}/validate?url=${encodeURIComponent(url)}&platform=${platform}`
    );
    return data?.valid === true;
  },
  
  // Хелпер: проверить, есть ли уже ссылка на эту платформу
  hasPlatform(links, platform) {
    return links?.some(link => link.platform === platform);
  },
  
  // Хелпер: отсортировать ссылки по displayOrder
  sortLinks(links) {
    if (!Array.isArray(links)) return [];
    return [...links].sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0));
  },
  
  // Хелпер: получить видимые ссылки
  getVisibleLinks(links) {
    if (!Array.isArray(links)) return [];
    return links.filter(link => link.visible !== false);
  },
  
  // Форматирование ссылки для отображения (полный URL + иконка)
  formatLinkForDisplay(link) {
    if (!link) return null;
    return {
      ...link,
      fullUrl: formatFullUrl(link.platform, link.url),
      icon: getPlatformIcon(link.platform),
      platformLabel: link.platformName || link.platform,
    };
  },
  
  // Парсинг юзернейма из полного URL (обратная операция)

  
  // === EXPORT UTILS ===
  
  utils: {
    formatLink,
    formatLinksList,
    getPlatformIcon,
    getPlatformBaseUrl,
    formatFullUrl,
    extractUsername,
  },
  
  // Константы платформ для удобного импорта в компонентах
  PLATFORMS: {
    TELEGRAM: 'TELEGRAM',
    VKONTAKTE: 'VKONTAKTE',
    YOUTUBE: 'YOUTUBE',
    TWITTER: 'TWITTER',
    TIKTOK: 'TIKTOK',
  },
};

export default linksApi;