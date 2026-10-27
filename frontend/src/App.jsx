import { useEffect, useState } from 'react'
import { fetchAttempt, fetchQuiz, submitQuiz } from './api.js'
import { useAuth } from './auth-context.js'
import { useTheme } from './hooks.js'
import ThemeToggle from './components/ThemeToggle.jsx'
import { AlertIcon } from './components/Icons.jsx'
import Home from './views/Home.jsx'
import Topics from './views/Topics.jsx'
import TopicIntro from './views/TopicIntro.jsx'
import Quiz from './views/Quiz.jsx'
import Result from './views/Result.jsx'
import Login from './views/Login.jsx'
import Dashboard from './views/Dashboard.jsx'
import MyAttempts from './views/MyAttempts.jsx'

function initialsOf(name) {
  return name
    .split(/\s+/)
    .map((word) => word[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

export default function App() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const [route, setRoute] = useState({ view: 'home' })
  const [attempt, setAttempt] = useState(0)
  const [starting, setStarting] = useState(false)
  const [startError, setStartError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(null)
  const [pendingRoute, setPendingRoute] = useState(null)
  const [attemptError, setAttemptError] = useState(null)

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [route.view, route.topic?.id])

  const goHome = () => setRoute({ view: 'home' })

  const openCourse = (course) => setRoute({ view: 'topics', course })

  const openTopic = (topic) =>
    setRoute((previous) => ({ ...previous, view: 'intro', topic, result: null }))

  const requireLogin = (returnTo) => {
    setPendingRoute(returnTo)
    setRoute({ view: 'login' })
  }

  const startQuiz = async (topic, course) => {
    if (!user) {
      requireLogin({ view: 'intro', course, topic })
      return
    }
    setStarting(true)
    setStartError(null)
    setSubmitError(null)
    try {
      const quiz = await fetchQuiz(topic.id)
      setAttempt((value) => value + 1)
      setRoute({ view: 'quiz', course, topic, quiz })
    } catch (error) {
      setStartError(error)
    } finally {
      setStarting(false)
    }
  }

  const handleSubmit = async (answers) => {
    setSubmitting(true)
    setSubmitError(null)
    try {
      const result = await submitQuiz(route.topic.id, answers)
      setRoute((previous) => ({
        view: 'result',
        course: previous.course,
        topic: previous.topic,
        result,
      }))
    } catch (error) {
      setSubmitError(error)
    } finally {
      setSubmitting(false)
    }
  }

  const openAttempt = async (attemptId) => {
    setAttemptError(null)
    try {
      const result = await fetchAttempt(attemptId)
      setRoute({
        view: 'attempt-review',
        result,
        backTo: user?.role === 'TEACHER' ? 'dashboard' : 'attempts',
      })
    } catch (error) {
      setAttemptError(error)
    }
  }

  const handleLogout = () => {
    logout()
    setPendingRoute(null)
    goHome()
  }

  return (
    <>
      <a className="skip-link" href="#main">
        Skip to content
      </a>

      <header className="site-header">
        <button type="button" className="brand" onClick={goHome} aria-label="Quizora home">
          <span className="brand-mark" aria-hidden="true">
            Q
          </span>
          <span className="brand-text">
            Quizora
            <span>course quizzes</span>
          </span>
        </button>

        <div className="header-actions">
          {user ? (
            <>
              {user.role === 'TEACHER' ? (
                <button
                  type="button"
                  className="btn ghost small"
                  onClick={() => setRoute({ view: 'dashboard' })}
                >
                  Dashboard
                </button>
              ) : (
                <button
                  type="button"
                  className="btn ghost small"
                  onClick={() => setRoute({ view: 'attempts' })}
                >
                  My attempts
                </button>
              )}
              <span className="user-chip" title={user.email}>
                <span className="avatar" aria-hidden="true">
                  {initialsOf(user.name)}
                </span>
                <span className="user-meta">
                  <strong>{user.name}</strong>
                  <span>{user.role === 'TEACHER' ? 'Teacher' : 'Student'}</span>
                </span>
              </span>
              <button type="button" className="btn ghost small" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <button type="button" className="btn ghost small" onClick={() => requireLogin(null)}>
              Log in
            </button>
          )}
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
        </div>
      </header>

      {attemptError && (
        <div className="page">
          <p className="inline-error" role="alert">
            <AlertIcon /> {attemptError.message}
          </p>
        </div>
      )}

      {route.view === 'home' && <Home onSelectCourse={openCourse} />}

      {route.view === 'login' && (
        <Login onDone={() => setRoute(pendingRoute ?? { view: 'home' })} />
      )}

      {route.view === 'attempts' &&
        (user ? (
          <MyAttempts onBack={goHome} onOpenAttempt={openAttempt} />
        ) : (
          <Login onDone={() => setRoute({ view: 'attempts' })} />
        ))}

      {route.view === 'dashboard' &&
        (user?.role === 'TEACHER' ? (
          <Dashboard user={user} onBack={goHome} onOpenAttempt={openAttempt} />
        ) : (
          <Login onDone={() => setRoute({ view: 'dashboard' })} />
        ))}

      {route.view === 'topics' && (
        <Topics course={route.course} onBack={goHome} onSelectTopic={openTopic} />
      )}

      {route.view === 'intro' && (
        <TopicIntro
          course={route.course}
          topic={route.topic}
          user={user}
          starting={starting}
          error={startError}
          onStart={() => startQuiz(route.topic, route.course)}
          onBack={() => setRoute({ view: 'topics', course: route.course })}
        />
      )}

      {route.view === 'quiz' && (
        <Quiz
          key={attempt}
          quiz={route.quiz}
          submitting={submitting}
          error={submitError}
          onSubmit={handleSubmit}
          onExit={() =>
            setRoute({ view: 'intro', course: route.course, topic: route.topic })
          }
        />
      )}

      {route.view === 'result' && (
        <Result
          result={route.result}
          course={route.course}
          onRetry={() => startQuiz(route.topic, route.course)}
          onBackToTopics={() => setRoute({ view: 'topics', course: route.course })}
          onHome={goHome}
        />
      )}

      {route.view === 'attempt-review' && (
        <Result
          result={route.result}
          course={null}
          onBackTo={() => setRoute({ view: route.backTo })}
          backLabel={route.backTo === 'dashboard' ? 'Back to dashboard' : 'Back to my attempts'}
          onHome={goHome}
        />
      )}

      <footer className="site-footer">
        <span>Spring Boot REST API · React frontend · scored on the server</span>
      </footer>
    </>
  )
}
