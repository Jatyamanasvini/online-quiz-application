import { fetchTeacherAttempts, fetchTeacherStats } from '../api.js'
import { useAsync } from '../hooks.js'
import { ArrowLeftIcon } from '../components/Icons.jsx'
import { TopicListSkeleton } from '../components/Skeletons.jsx'
import { EmptyState, ErrorState } from '../components/States.jsx'

export default function Dashboard({ user, onBack, onOpenAttempt }) {
  const stats = useAsync(fetchTeacherStats, [])
  const attempts = useAsync(fetchTeacherAttempts, [])

  const rows = attempts.data ?? []
  const topics = stats.data ?? []
  const uniqueStudents = new Set(rows.map((row) => row.studentEmail)).size
  const averageAccuracy = rows.length
    ? Math.round(rows.reduce((sum, row) => sum + row.accuracy, 0) / rows.length)
    : 0

  return (
    <main id="main" className="page">
      <button type="button" className="btn ghost back-link" onClick={onBack}>
        <ArrowLeftIcon /> Home
      </button>

      <header className="course-hero">
        <div>
          <p className="eyebrow">Teacher · {user.name}</p>
          <h1>Class dashboard</h1>
          <p className="lede">Every student attempt across all topics.</p>
        </div>
      </header>

      <dl className="hero-stats dashboard-stats">
        <div>
          <dt>Attempts</dt>
          <dd>{attempts.status === 'success' ? rows.length : '—'}</dd>
        </div>
        <div>
          <dt>Students</dt>
          <dd>{attempts.status === 'success' ? uniqueStudents : '—'}</dd>
        </div>
        <div>
          <dt>Average accuracy</dt>
          <dd>{attempts.status === 'success' ? `${averageAccuracy}%` : '—'}</dd>
        </div>
        <div>
          <dt>Topics attempted</dt>
          <dd>{stats.status === 'success' ? topics.length : '—'}</dd>
        </div>
      </dl>

      <section className="section" aria-labelledby="perf-heading">
        <div className="section-head">
          <h2 id="perf-heading">Topic performance</h2>
          {stats.status === 'success' && <span className="muted">{topics.length} topics</span>}
        </div>

        {stats.status === 'loading' && <TopicListSkeleton />}
        {stats.status === 'error' && (
          <ErrorState title="Could not load statistics" message={stats.error?.message} onRetry={stats.retry} />
        )}
        {stats.status === 'success' && topics.length === 0 && (
          <EmptyState title="No attempts yet" message="Topic statistics appear once students submit quizzes." />
        )}
        {stats.status === 'success' && topics.length > 0 && (
          <div className="table-card">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col">Topic</th>
                  <th scope="col">Attempts</th>
                  <th scope="col">Students</th>
                  <th scope="col">Avg. accuracy</th>
                </tr>
              </thead>
              <tbody>
                {topics.map((topic) => (
                  <tr key={topic.topicId}>
                    <td>{topic.topicTitle}</td>
                    <td>{topic.attempts}</td>
                    <td>{topic.students}</td>
                    <td>
                      <span className="stat-bar" aria-hidden="true">
                        <span style={{ width: `${Math.min(100, topic.averageAccuracy)}%` }} />
                      </span>
                      <strong>{topic.averageAccuracy}%</strong>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="section" aria-labelledby="recent-heading">
        <div className="section-head">
          <h2 id="recent-heading">Recent attempts</h2>
          {attempts.status === 'success' && <span className="muted">{rows.length} total</span>}
        </div>

        {attempts.status === 'loading' && <TopicListSkeleton />}
        {attempts.status === 'error' && (
          <ErrorState
            title="Could not load attempts"
            message={attempts.error?.message}
            onRetry={attempts.retry}
          />
        )}
        {attempts.status === 'success' && rows.length === 0 && (
          <EmptyState title="No submissions yet" message="Attempts appear here as soon as a student submits a quiz." />
        )}
        {attempts.status === 'success' && rows.length > 0 && (
          <div className="table-card">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col">Student</th>
                  <th scope="col">Topic</th>
                  <th scope="col">Score</th>
                  <th scope="col">Accuracy</th>
                  <th scope="col">Submitted</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr
                    key={row.id}
                    className="clickable-row"
                    onClick={() => onOpenAttempt(row.id)}
                    onKeyDown={(event) => event.key === 'Enter' && onOpenAttempt(row.id)}
                    tabIndex={0}
                    role="button"
                    aria-label={`View attempt by ${row.studentName} on ${row.topicTitle}`}
                  >
                    <td>
                      <strong>{row.studentName}</strong>
                      <span className="row-sub">{row.studentEmail}</span>
                    </td>
                    <td>{row.topicTitle}</td>
                    <td>
                      {row.correct}/{row.total}
                    </td>
                    <td>
                      <span className={row.accuracy >= 80 ? 'ok-text' : row.accuracy >= 50 ? '' : 'bad-text'}>
                        {Math.round(row.accuracy)}%
                      </span>
                    </td>
                    <td>{new Date(row.submittedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}
