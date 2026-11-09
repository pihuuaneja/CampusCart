import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await login(form.email, form.password)
      navigate(location.state?.from || '/', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-panel">
      <h1>Log in</h1>
      <p className="muted">Log in with your @thapar.edu email.</p>
      {error && <p className="alert" role="alert">{error}</p>}
      <form onSubmit={submit} className="form">
        <div className="field">
          <label htmlFor="email">Thapar email</label>
          <input id="email" type="email" autoComplete="email" placeholder="you@thapar.edu" required value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete="current-password" required value={form.password}
                 onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </div>
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
      </form>
      <p className="muted">New here? <Link to="/register">Create an account</Link></p>
    </div>
  )
}
