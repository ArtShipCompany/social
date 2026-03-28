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
    const [isInitializing, setIsInitializing] = useState(true);
    const [isAuthChecked, setIsAuthChecked] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);

    useEffect(() => {
        const loadUser = () => {
            // console.log('🔄 AuthProvider: loading user...');
            try {
                const token = getAuthToken();
                // console.log('🔑 Token from localStorage:', token ? 'Exists' : 'None');
                
                if (token && isAuthenticated()) {
                    const userData = getCurrentUser();
                    // console.log('👤 User data from localStorage:', userData);
                    
                    if (userData && userData.id) {
                        setUser(userData);
                        // console.log('✅ User loaded:', userData.username);
                    } else {
                        // console.log('⚠️ User data invalid or missing');
                        setUser(null);
                    }
                } else {
                    // console.log('⚠️ No valid token or not authenticated');
                    setUser(null);
                }
            } catch (error) {
                // console.error('❌ Error loading user:', error);
                setUser(null);
            } finally {
                setIsInitializing(false);
                setIsAuthChecked(true);
                // console.log('🏁 AuthProvider: initialization complete');
            }
        };

        loadUser();
        
        // Слушаем изменения localStorage
        const handleStorageChange = (e) => {
            if (e.key === 'accessToken' || e.key === 'user') {
                // console.log('📦 LocalStorage changed:', e.key);
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
            setIsProcessing(true);
            const response = await authApi.login(credentials);
            
            // Получаем пользователя после успешного логина
            const userData = getCurrentUser();
            
            if (userData) {
                setUser(userData);
                // console.log('✅ User set after login:', userData.username);
            }
            
            return { success: true, data: response, user: userData };
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, error: error.message };
        } finally {
            setIsProcessing(false);
        }
    };

    const logout = async () => {
        try {
            setIsProcessing(true);
            await authApi.logout();
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            setUser(null);
            setIsProcessing(false);
            // console.log('✅ User logged out');
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
                // console.log('✅ User refreshed:', userData.username);
            } else {
                setUser(null);
                // console.log('⚠️ User refresh failed - invalid data');
            }
        } catch (error) {
            console.error('Error refreshing user:', error);
            setUser(null);
        }
    };

    const verifyEmail = async (token) => {
        try {
            const response = await authApi.verifyEmail(token);
            return { success: true, data: response };
        } catch (error) {
            console.error('❌ Verify email error:', error);
            return { 
                success: false, 
                error: error.message || 'Не удалось подтвердить email' 
            };
        }
    };

    const resendVerification = async (email) => {
        try {
            const response = await authApi.resendVerification(email);
            return { success: true, data: response };
        } catch (error) {
            console.error('❌ Resend verification error:', error);
            return { 
                success: false, 
                error: error.message || 'Не удалось отправить письмо' 
            };
        }
    };

    const checkVerificationStatus = async (email) => {
        try {
            const response = await authApi.checkVerificationStatus(email);
            return { success: true, data: response };
        } catch (error) {
            console.error('❌ Check verification status error:', error);
            return { 
                success: false, 
                error: error.message || 'Не удалось проверить статус' 
            };
        }
    };
    const value = {
        user,
        isLoading: isInitializing,
        isProcessing,
        isAuthChecked,
        isAuthenticated: !!user && isAuthenticated(),
        
        login,
        logout,
        register,

        verifyEmail,
        resendVerification,
        checkVerificationStatus,

        setUser,
        refreshUser 
    };

    // console.log('AuthContext value:', {
    //     user: user?.username,
    //     isAuthenticated: value.isAuthenticated,
    //     // isLoading
    // });

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};