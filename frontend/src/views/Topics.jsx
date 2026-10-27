import { fetchTopics } from '../api.js'
import { useAsync } from '../hooks.js'
import { ArrowLeftIcon, ArrowRightIcon } from '../components/Icons.jsx'
import { TopicListSkeleton } from '../components/Skeletons.jsx'
import { EmptyState, ErrorState } from '../components/States.jsx'

export default function Topics({ course, onBack, onSelectTopic }) {
  const { status, data, error, retry } = useAsync(
    () => fetchTopics(course.id),
    [course.id],
  )
  const topics = data ?? []
  const accent = course.accent || 'var(--accent)'

  return (
    <main id="main" className="page">
      <button type="button" className="btn ghost back-link" onClick={onBack}>
        <ArrowLeftIcon /> All courses
      </button>

      <header className="course-hero" style={{ '--card-accent': accent }}>
        <span className="course-icon large" aria-hidden="true">
          {course.icon}
        </span>
        <div>
          <p className="eyebrow">{course.title} · topics</p>
          <h1>{course.title}</h1>
          <p className="lede">{course.description}</p>
        </div>
      </header>

      <section className="section" aria-labelledby="topics-heading">
        <div className="section-head">
          <h2 id="topics-heading">Choose a topic</h2>
          {status === 'success' && <span className="muted">{topics.length} topics</span>}
        </div>

        {status === 'loading' && <TopicListSkeleton />}

        {status === 'error' && (
          <ErrorState
            title="Could not load topics"
            message={error?.message}
            onRetry={retry}
          />
        )}

        {status === 'success' && topics.length === 0 && (
          <EmptyState
            title="No topics in this course"
            message="This course has not been populated yet."
          />
        )}

        {status === 'success' && topics.length > 0 && (
          <div className="topic-list">
            {topics.map((topic, index) => {
              const available = topic.questionCount > 0
              return (
                <button
                  key={topic.id}
                  type="button"
                  className={`topic-row${available ? '' : ' is-disabled'}`}
                  style={{ '--card-accent': accent }}
                  onClick={() => onSelectTopic(topic)}
                  disabled={!available}
                  aria-label={
                    available
                      ? `${topic.title}, ${topic.questionCount} questions`
                      : `${topic.title}, questions coming soon`
                  }
                >
                  <span className="topic-index" aria-hidden="true">
                    {String(index + 1).padStart(2, '0')}
                  </span>
                  <span className="topic-body">
                    <span className="topic-title">{topic.title}</span>
                    <span className="topic-desc">{topic.description}</span>
                  </span>
                  <span className={available ? 'badge' : 'badge badge-muted'}>
                    {available ? `${topic.questionCount} questions` : 'Coming soon'}
                  </span>
                  <span className="topic-arrow" aria-hidden="true">
                    <ArrowRightIcon />
                  </span>
                </button>
              )
            })}
          </div>
        )}
      </section>
    </main>
  )
}
