import { useState } from 'react'
import { useAuth } from '../auth-context.js'
import { AlertIcon } from '../components/Icons.jsx'

export default function Login({ onDone }) {
  const { login, register } = useAuth()
  const [mode, setMode] = useState('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      if (mode === 'login') {
        await login(email, password)
      } else {
        await register(name, email, password)
      }
      onDone()
    } catch (err) {
      setError(err)
    } finally {
      setPending(false)
    }
  }

  const switchMode = (next) => {
    setMode(next)
    setError(null)
  }

  return (
    <main id="main" className="page page-narrow">
      <section className="intro-card auth-card">
        <p className="eyebrow">Quizora account</p>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
        <p className="lede">
          Log in to take quizzes and keep a history of your attempts. Teachers can review
          every student's results.
        </p>

        <div className="auth-tabs" role="tablist" aria-label="Authentication mode">
          <button
            type="button"
            role="tab"
            aria-selected={mode === 'login'}
            className={`tab${mode === 'login' ? ' active' : ''}`}
            onClick={() => switchMode('login')}
          >
            Log in
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={mode === 'register'}
            className={`tab${mode === 'register' ? ' active' : ''}`}
            onClick={() => switchMode('register')}
          >
            Register
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          {mode === 'register' && (
            <label className="field">
              <span>Name</span>
              <input
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
                minLength={2}
                placeholder="Ada Lovelace"
                autoComplete="name"
              />
            </label>
          )}
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              placeholder="you@example.com"
              autoComplete="email"
            />
          </label>
          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={6}
              placeholder="At least 6 characters"
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            />
          </label>

          {error && (
            <p className="inline-error" role="alert">
              <AlertIcon /> {error.message}
            </p>
          )}

          <button type="submit" className="btn primary large" disabled={pending}>
            {pending
              ? mode === 'login'
                ? 'Logging in…'
                : 'Creating account…'
              : mode === 'login'
                ? 'Log in'
                : 'Create account'}
          </button>
        </form>

        <div className="demo-box">
          <p>
            <strong>Demo accounts</strong>
          </p>
          <p>
            <code>teacher@quizora.local</code> · <code>teacher123</code> — teacher dashboard
          </p>
          <p>
            <code>student@quizora.local</code> · <code>student123</code> — student
          </p>
        </div>
      </section>
    </main>
  )
}
