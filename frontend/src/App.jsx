import { useEffect, useState } from 'react'
import { fetchQuiz, submitQuiz } from './api.js'
import { useTheme } from './hooks.js'
import ThemeToggle from './components/ThemeToggle.jsx'
import Home from './views/Home.jsx'
import Topics from './views/Topics.jsx'
import TopicIntro from './views/TopicIntro.jsx'
import Quiz from './views/Quiz.jsx'
import Result from './views/Result.jsx'

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const [route, setRoute] = useState({ view: 'home' })
  const [attempt, setAttempt] = useState(0)
  const [starting, setStarting] = useState(false)
  const [startError, setStartError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(null)

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [route.view, route.topic?.id])

  const goHome = () => setRoute({ view: 'home' })

  const openCourse = (course) => setRoute({ view: 'topics', course })

  const openTopic = (topic) =>
    setRoute((previous) => ({ ...previous, view: 'intro', topic, result: null }))

  const startQuiz = async (topic, course) => {
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
        <ThemeToggle theme={theme} onToggle={toggleTheme} />
      </header>

      {route.view === 'home' && <Home onSelectCourse={openCourse} />}

      {route.view === 'topics' && (
        <Topics course={route.course} onBack={goHome} onSelectTopic={openTopic} />
      )}

      {route.view === 'intro' && (
        <TopicIntro
          course={route.course}
          topic={route.topic}
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

      <footer className="site-footer">
        <span>Spring Boot REST API · React frontend · scored on the server</span>
      </footer>
    </>
  )
}
