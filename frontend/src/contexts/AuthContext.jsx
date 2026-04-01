import React, { createContext, useState, useContext, useEffect } from 'react';
import { authApi, isAuthenticated, getCurrentUser, getAuthToken, fetchWithErrorHandling, API_URL } from '../api/authApi';

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
    const loadUser = async () => {
        try {
            const token = getAuthToken();
            
            if (token) {
                const response = await fetchWithErrorHandling(`${API_URL}/users/me`, {
                    method: 'GET',
                });
                
                if (response?.id) {
                    localStorage.setItem('user', JSON.stringify(response));
                    setUser(response);
                } else {
                    throw new Error('Invalid user data');
                }
            } else {
                setUser(null);
            }
        } catch (error) {
            if (error.status === 401) {
                console.log('🔄 Access token expired, trying silent refresh...');
                const refreshed = await authApi.refreshToken();
                
                if (refreshed) {
                    try {
                        const response = await fetchWithErrorHandling(`${API_URL}/users/me`);
                        if (response?.id) {
                            localStorage.setItem('user', JSON.stringify(response));
                            setUser(response);
                        }
                    } catch {
                        setUser(null);
                        clearAuthStorage();
                    }
                } else {
                    setUser(null);
                    clearAuthStorage();
                }
            } else {
                console.error('❌ Auth init error:', error);
                setUser(null);
                clearAuthStorage();
            }
        } finally {
            setIsInitializing(false);
            setIsAuthChecked(true);
        }
    };

    loadUser();

        const handleStorageChange = (e) => {
            if (e.key === 'accessToken' && !e.newValue) {
                setUser(null);
            }
        };
        window.addEventListener('storage', handleStorageChange);

        const handleGlobalLogout = () => {
            console.log('🔔 AuthProvider: received auth:logout event');
            setUser(null);
        };
        window.addEventListener('auth:logout', handleGlobalLogout);
        
        return () => {
            window.removeEventListener('storage', handleStorageChange);
            window.removeEventListener('auth:logout', handleGlobalLogout);
        }
    }, []);

    //LOGIN
    const login = async (credentials) => {
        try {
            setIsProcessing(true);
            const response = await authApi.login(credentials);
            
            const userData = getCurrentUser();
            if (userData) setUser(userData);
            
            return { success: true, data: response, user: userData };
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, error: error.message };
        } finally {
            setIsProcessing(false);
        }
    };

    // LOGOUT
    const logout = async () => {
        try {
            setIsProcessing(true);
            await authApi.logout();
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            setUser(null);
            setIsProcessing(false);
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
        isAuthenticated: !!user,
        
        login,
        logout,
        register,

        verifyEmail,
        resendVerification,
        checkVerificationStatus,

        setUser,
        refreshUser 
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};