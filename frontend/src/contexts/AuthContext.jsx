// AuthContext.jsx
import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { authApi, isAuthenticated, getCurrentUser, setAuthToken } from '../api/authApi';

const AuthContext = createContext({});

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isAuthChecked, setIsAuthChecked] = useState(false);

    // Загрузка пользователя при инициализации
    useEffect(() => {
        const loadUser = async () => {
            try {
                setIsLoading(true);
                
                // Проверяем токен и получаем пользователя из localStorage
                if (isAuthenticated()) {
                    const userData = getCurrentUser();
                    if (userData) {
                        setUser(userData);
                        console.log('✅ Пользователь загружен из localStorage:', userData.username);
                    }
                } else {
                    console.log('⚠️ Пользователь не авторизован');
                    setUser(null);
                }
            } catch (error) {
                console.error('Ошибка при загрузке пользователя:', error);
                setUser(null);
            } finally {
                setIsLoading(false);
                setIsAuthChecked(true);
            }
        };

        loadUser();
        
        // Слушаем изменения localStorage (на случай если логин из другого окна)
        const handleStorageChange = (e) => {
            if (e.key === 'accessToken' || e.key === 'user') {
                loadUser();
            }
        };
        
        window.addEventListener('storage', handleStorageChange);
        
        return () => {
            window.removeEventListener('storage', handleStorageChange);
        };
    }, []);

    const login = async (credentials) => {
        try {
            setIsLoading(true);
            const response = await authApi.login(credentials);
            
            // Получаем пользователя после успешного логина
            const userData = getCurrentUser();
            if (userData) {
                setUser(userData);
                console.log('✅ Пользователь установлен после логина:', userData.username);
            }
            
            return { success: true, data: response, user: userData };
        } catch (error) {
            console.error('Ошибка при логине:', error);
            return { success: false, error: error.message };
        } finally {
            setIsLoading(false);
        }
    };

    const logout = async () => {
        try {
            await authApi.logout();
        } catch (error) {
            console.error('Ошибка при выходе:', error);
        } finally {
            setUser(null);
            setIsLoading(false);
        }
    };

    const register = async (userData) => {
        try {
            const response = await authApi.register(userData);
            return { success: true, data: response };
        } catch (error) {
            return { success: false, error: error.message };
        }
    };

    const value = {
        user,
        isLoading,
        isAuthChecked,
        isAuthenticated: !!user,
        login,
        logout,
        register,
        setUser // для обновления данных пользователя
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};