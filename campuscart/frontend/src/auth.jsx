import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import api, { tokenStore } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(!!tokenStore.get())

  useEffect(() => {
    if (!tokenStore.get()) return
    api.get('/auth/me')
      .then((r) => setUser(r.data))
      .catch(() => tokenStore.clear())
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    tokenStore.set(data.token)
    setUser(data.user)
  }, [])

  const register = useCallback(async (form) => {
    const { data } = await api.post('/auth/register', form)
    tokenStore.set(data.token)
    setUser(data.user)
  }, [])

  const logout = useCallback(() => {
    tokenStore.clear()
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, loading, login, register, logout }), [user, loading, login, register, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
