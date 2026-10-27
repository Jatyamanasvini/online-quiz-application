import { useEffect, useState } from 'react'
import {
  AUTH_EXPIRED_EVENT,
  clearToken,
  fetchMe,
  getToken,
  login as apiLogin,
  register as apiRegister,
  setToken,
} from './api.js'
import { AuthContext } from './auth-context.js'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  // Restoring a session is only needed when a token is already stored.
  const [ready, setReady] = useState(() => !getToken())

  useEffect(() => {
    if (!getToken()) {
      return
    }
    let cancelled = false
    fetchMe()
      .then((restored) => {
        if (!cancelled) setUser(restored)
      })
      .catch(() => {
        // A real 401 clears the token inside the API layer. Network failures keep it,
        // so a backend restart does not silently log the user out.
      })
      .finally(() => {
        if (!cancelled) setReady(true)
      })
    return () => {
      cancelled = true
    }
  }, [])

  // The API layer clears the stored token when the server rejects it (expired or
  // revoked). Mirror that in React state so the header returns to "Log in" too.
  useEffect(() => {
    const handleExpired = () => {
      setUser(null)
      setReady(true)
    }
    window.addEventListener(AUTH_EXPIRED_EVENT, handleExpired)
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, handleExpired)
  }, [])

  const login = async (email, password) => {
    const response = await apiLogin(email, password)
    setToken(response.token)
    setUser(response.user)
    setReady(true)
    return response.user
  }

  const register = async (name, email, password) => {
    const response = await apiRegister(name, email, password)
    setToken(response.token)
    setUser(response.user)
    setReady(true)
    return response.user
  }

  const logout = () => {
    clearToken()
    setUser(null)
    setReady(true)
  }

  return (
    <AuthContext.Provider value={{ user, ready, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
