import { useState, useCallback, useRef } from 'react';
import { authApi, clearAuthStorage } from '../api/authApi';

export const useApi = (apiModule = null) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const abortControllerRef = useRef(null);

  const request = useCallback(async (url, options = {}, { retry = true } = {}) => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);

    try {
      const data = await authApi.fetchProtected(url, {
        ...options,
        signal: abortControllerRef.current.signal,
      });

      return { data, error: null };
    } catch (err) {
      if (err.name === 'AbortError') {
        return { data: null, error: null };
      }

      setError(err);

      if (err.status === 401 && retry) {
        clearAuthStorage();
        window.dispatchEvent(new CustomEvent('auth:logout'));
      }

      return { data: null, error: err };
    } finally {
      setLoading(false);
    }
  }, []);

  const callApiMethod = useCallback(async (methodName, ...args) => {
    if (!apiModule || !apiModule[methodName]) {
      const err = new Error(`Method ${methodName} not found in API module`);
      setError(err);
      return { data: null, error: err };
    }

    setLoading(true);
    setError(null);

    try {
      const data = await apiModule[methodName](...args);
      return { data, error: null };
    } catch (err) {
      setError(err);
      
      if (err.status === 401) {
        const refreshed = await authApi.refreshToken();
        if (refreshed) {
          try {
            const data = await apiModule[methodName](...args);
            return { data, error: null };
          } catch (retryErr) {
            setError(retryErr);
            if (retryErr.status === 401) {
              clearAuthStorage();
              window.dispatchEvent(new CustomEvent('auth:logout'));
            }
          }
        } else {
          clearAuthStorage();
          window.dispatchEvent(new CustomEvent('auth:logout'));
        }
      }
      
      return { data: null, error: err };
    } finally {
      setLoading(false);
    }
  }, [apiModule]);

  const get = useCallback((url, opts) => request(url, { ...opts, method: 'GET' }), [request]);
  const post = useCallback((url, body, opts) => 
    request(url, { ...opts, method: 'POST', body: JSON.stringify(body) }), [request]);
  const put = useCallback((url, body, opts) => 
    request(url, { ...opts, method: 'PUT', body: JSON.stringify(body) }), [request]);
  const del = useCallback((url, opts) => request(url, { ...opts, method: 'DELETE' }), [request]);

  const cancel = useCallback(() => {
    abortControllerRef.current?.abort();
  }, []);

  return {
    loading,
    error,
    
    request,
    callApiMethod,
    get,
    post,
    put,
    del,
    
    cancel,
    clearError: () => setError(null),
    reset: () => {
      setError(null);
      setLoading(false);
    }
  };
};