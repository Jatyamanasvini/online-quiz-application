import { ArrowLeftIcon, AlertIcon, PlayIcon } from '../components/Icons.jsx'

export default function TopicIntro({ course, topic, onBack, onStart, starting, error }) {
  const available = topic.questionCount > 0
  const accent = course.accent || '#6366f1'

  return (
    <main id="main" className="page page-narrow">
      <button type="button" className="btn ghost back-link" onClick={onBack}>
        <ArrowLeftIcon /> {course.title}
      </button>

      <section className="intro-card" style={{ '--card-accent': accent }}>
        <p className="eyebrow">{course.title}</p>
        <h1>{topic.title}</h1>
        <p className="lede">{topic.description}</p>

        <dl className="intro-facts">
          <div>
            <dt>Questions</dt>
            <dd>{topic.questionCount}</dd>
          </div>
          <div>
            <dt>Format</dt>
            <dd>One at a time</dd>
          </div>
          <div>
            <dt>Scoring</dt>
            <dd>Server-side</dd>
          </div>
        </dl>

        <ul className="intro-points">
          <li>Move freely between questions with Previous and Next.</li>
          <li>Answers stay hidden until you submit — then every explanation unlocks.</li>
          <li>No timer, so you can think before committing.</li>
        </ul>

        {error && (
          <p className="inline-error" role="alert">
            <AlertIcon /> {error.message}
          </p>
        )}

        {available ? (
          <button
            type="button"
            className="btn primary large"
            onClick={onStart}
            disabled={starting}
          >
            <PlayIcon />
            {starting ? 'Loading quiz…' : 'Start quiz'}
          </button>
        ) : (
          <p className="notice">
            Questions for this topic are coming soon. Pick another topic to practise.
          </p>
        )}
      </section>
    </main>
  )
}
