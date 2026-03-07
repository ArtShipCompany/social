import React, { createContext, useState, useContext, useEffect } from 'react';
import { authApi, isAuthenticated, getCurrentUser, getAuthToken } from '../api/authApi';

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

    useEffect(() => {
        const loadUser = () => {
            console.log('🔄 AuthProvider: loading user...');
            try {
                const token = getAuthToken();
                console.log('🔑 Token from localStorage:', token ? 'Exists' : 'None');
                
                if (token && isAuthenticated()) {
                    const userData = getCurrentUser();
                    console.log('👤 User data from localStorage:', userData);
                    
                    if (userData && userData.id) {
                        setUser(userData);
                        console.log('✅ User loaded:', userData.username);
                    } else {
                        console.log('⚠️ User data invalid or missing');
                        setUser(null);
                    }
                } else {
                    console.log('⚠️ No valid token or not authenticated');
                    setUser(null);
                }
            } catch (error) {
                console.error('❌ Error loading user:', error);
                setUser(null);
            } finally {
                setIsLoading(false);
                setIsAuthChecked(true);
                console.log('🏁 AuthProvider: initialization complete');
            }
        };

        loadUser();
        
        // Слушаем изменения localStorage
        const handleStorageChange = (e) => {
            if (e.key === 'accessToken' || e.key === 'user') {
                console.log('📦 LocalStorage changed:', e.key);
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
                console.log('✅ User set after login:', userData.username);
            }
            
            return { success: true, data: response, user: userData };
        } catch (error) {
            console.error('❌ Login error:', error);
            return { success: false, error: error.message };
        } finally {
            setIsLoading(false);
        }
    };

    const logout = async () => {
        try {
            await authApi.logout();
        } catch (error) {
            console.error('❌ Logout error:', error);
        } finally {
            setUser(null);
            setIsLoading(false);
            console.log('✅ User logged out');
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

    const refreshUser = () => {
        console.log('🔄 AuthProvider: manually refreshing user...');
        try {
            const token = getAuthToken();
            const userData = getCurrentUser();
            
            if (token && isAuthenticated() && userData && userData.id) {
                setUser(userData);
                console.log('✅ User refreshed:', userData.username);
            } else {
                setUser(null);
                console.log('⚠️ User refresh failed - invalid data');
            }
        } catch (error) {
            console.error('❌ Error refreshing user:', error);
            setUser(null);
        }
    };

    // Добавьте refreshUser в value:
    const value = {
        user,
        isLoading,
        isAuthChecked,
        isAuthenticated: !!user && isAuthenticated(),
        login,
        logout,
        register,
        setUser,
        refreshUser 
    };

    console.log('AuthContext value:', {
        user: user?.username,
        isAuthenticated: value.isAuthenticated,
        isLoading
    });

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};