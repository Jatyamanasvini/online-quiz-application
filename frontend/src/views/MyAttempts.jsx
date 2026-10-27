import { fetchMyAttempts } from '../api.js'
import { useAsync } from '../hooks.js'
import { ArrowLeftIcon } from '../components/Icons.jsx'
import { TopicListSkeleton } from '../components/Skeletons.jsx'
import { EmptyState, ErrorState } from '../components/States.jsx'

export default function MyAttempts({ onBack, onOpenAttempt }) {
  const { status, data, error, retry } = useAsync(fetchMyAttempts, [])
  const attempts = data ?? []

  return (
    <main id="main" className="page">
      <button type="button" className="btn ghost back-link" onClick={onBack}>
        <ArrowLeftIcon /> Home
      </button>

      <header className="course-hero">
        <div>
          <p className="eyebrow">Your progress</p>
          <h1>My attempts</h1>
          <p className="lede">Every quiz you submitted, newest first.</p>
        </div>
      </header>

      <section className="section" aria-labelledby="attempts-heading">
        <div className="section-head">
          <h2 id="attempts-heading">History</h2>
          {status === 'success' && <span className="muted">{attempts.length} attempts</span>}
        </div>

        {status === 'loading' && <TopicListSkeleton />}

        {status === 'error' && (
          <ErrorState
            title="Could not load your attempts"
            message={error?.message}
            onRetry={retry}
          />
        )}

        {status === 'success' && attempts.length === 0 && (
          <EmptyState
            title="No attempts yet"
            message="Take a quiz and your score will appear here."
          />
        )}

        {status === 'success' && attempts.length > 0 && (
          <div className="topic-list">
            {attempts.map((attempt) => (
              <button
                key={attempt.id}
                type="button"
                className="topic-row attempt-row"
                onClick={() => onOpenAttempt(attempt.id)}
                aria-label={`${attempt.courseTitle} ${attempt.topicTitle}, score ${attempt.correct} of ${attempt.total}`}
              >
                <span className="score-chip">
                  {attempt.correct}/{attempt.total}
                </span>
                <span className="topic-body">
                  <span className="topic-title">
                    {attempt.courseTitle} · {attempt.topicTitle}
                  </span>
                  <span className="topic-desc">
                    {new Date(attempt.submittedAt).toLocaleString()}
                  </span>
                </span>
                <span className={attempt.accuracy >= 80 ? 'badge' : 'badge badge-muted'}>
                  {Math.round(attempt.accuracy)}% accuracy
                </span>
              </button>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}
