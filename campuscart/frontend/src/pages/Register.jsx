import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', hostel: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const bind = (key) => ({ value: form[key], onChange: (e) => setForm({ ...form, [key]: e.target.value }) })

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await register(form)
      navigate('/', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-panel">
      <h1>Create your account</h1>
      <p className="muted">Only students with a @thapar.edu email can join, which keeps every trade inside campus.</p>
      {error && <p className="alert" role="alert">{error}</p>}
      <form onSubmit={submit} className="form">
        <div className="field">
          <label htmlFor="name">Full name</label>
          <input id="name" autoComplete="name" required maxLength={80} {...bind('name')} />
        </div>
        <div className="field">
          <label htmlFor="email">Thapar email</label>
          <input id="email" type="email" autoComplete="email" placeholder="you@thapar.edu" required
                 pattern="[^@\s]+@([A-Za-z0-9\-]+\.)*[Tt]hapar\.edu" title="Use your @thapar.edu email address" {...bind('email')} />
        </div>
        <div className="field">
          <label htmlFor="hostel">Hostel or block</label>
          <input id="hostel" placeholder="For example, Block C" maxLength={80} {...bind('hostel')} />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete="new-password" required minLength={8} maxLength={72} {...bind('password')} />
          <small className="muted">At least 8 characters.</small>
        </div>
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
      </form>
      <p className="muted">Already registered? <Link to="/login">Log in</Link></p>
    </div>
  )
}
