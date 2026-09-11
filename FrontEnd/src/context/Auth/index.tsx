/* eslint-disable @typescript-eslint/no-empty-function */
import React, { createContext, useState, SetStateAction } from 'react';
import { useLocalStorage } from '../../hooks';
import { logout as logoutRequest } from '../../utils';

export const AuthContext = createContext<{
  isAuthenticated: boolean;
  setIsAuthenticated: React.Dispatch<SetStateAction<boolean>>;
  logout: () => void;
}>({
  isAuthenticated: false,
  setIsAuthenticated: () => {},
  logout: () => {},
});

const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [token, setToken] = useLocalStorage('token');
  const [isAuthenticated, setIsAuthenticated] = useState(!!token);

  const logout = () => {
  const clearSession = () => {
    setIsAuthenticated(false);
    setToken(null);
  };

  if (!token) {
    clearSession();
    return;
  }

  logoutRequest(token)
    .then(clearSession)
    .catch(clearSession);
  };

  return React.createElement(
    AuthContext.Provider,
    {
      value: { isAuthenticated, setIsAuthenticated, logout },
    },
    children
  );
};

export default AuthProvider;
