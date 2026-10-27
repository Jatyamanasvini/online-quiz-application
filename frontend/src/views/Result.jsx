import { CheckIcon, CloseIcon } from '../components/Icons.jsx'

function verdict(accuracy) {
  if (accuracy >= 80) return 'Excellent work'
  if (accuracy >= 50) return 'Solid attempt'
  return 'Keep practising'
}

export default function Result({ result, course, onRetry, onBackToTopics, onHome }) {
  const percent = Math.round(result.accuracy)
  const accent = course?.accent || '#6366f1'

  return (
    <main id="main" className="page">
      <section className="result-hero" style={{ '--card-accent': accent }}>
        <div
          className="score-ring"
          style={{ '--pct': percent }}
          role="img"
          aria-label={`Accuracy ${result.accuracy} percent`}
        >
          <div className="score-inner">
            <strong>{percent}%</strong>
            <span>accuracy</span>
          </div>
        </div>

        <div className="result-summary">
          <p className="eyebrow">{result.topicTitle}</p>
          <h1>{verdict(result.accuracy)}</h1>
          <p className="lede">
            You answered {result.correct} of {result.total} questions correctly.
          </p>

          <dl className="result-stats">
            <div>
              <dt>Correct</dt>
              <dd className="ok-text">{result.correct}</dd>
            </div>
            <div>
              <dt>Incorrect</dt>
              <dd className="bad-text">{result.incorrect}</dd>
            </div>
            <div>
              <dt>Unanswered</dt>
              <dd>{result.unanswered}</dd>
            </div>
            <div>
              <dt>Score</dt>
              <dd>
                {result.correct}/{result.total}
              </dd>
            </div>
          </dl>

          <div className="result-actions">
            <button type="button" className="btn primary" onClick={onRetry}>
              Try again
            </button>
            <button type="button" className="btn ghost" onClick={onBackToTopics}>
              Back to topics
            </button>
            <button type="button" className="btn ghost" onClick={onHome}>
              All courses
            </button>
          </div>
        </div>
      </section>

      <section className="section" aria-labelledby="review-heading">
        <div className="section-head">
          <h2 id="review-heading">Answer review</h2>
          <span className="muted">{result.results.length} questions</span>
        </div>

        <div className="review-list">
          {result.results.map((item, index) => (
            <article
              key={item.questionId}
              className={`review-item ${item.correct ? 'is-correct' : 'is-wrong'}`}
            >
              <header className="review-head">
                <span className={`pill ${item.correct ? 'ok' : 'bad'}`}>
                  {item.correct ? <CheckIcon /> : <CloseIcon />}
                  {item.correct ? 'Correct' : 'Incorrect'}
                </span>
                <h3>
                  <span className="review-index">{index + 1}.</span> {item.questionText}
                </h3>
              </header>

              <ul className="review-options">
                {item.options.map((option) => {
                  const isCorrectOption = option.id === item.correctOptionId
                  const isChosen = option.id === item.selectedOptionId
                  return (
                    <li
                      key={option.id}
                      className={`${isCorrectOption ? 'is-correct' : ''}${
                        isChosen && !isCorrectOption ? ' is-wrong' : ''
                      }`}
                    >
                      <span className="marker" aria-hidden="true">
                        {isCorrectOption ? '✓' : isChosen ? '✕' : ''}
                      </span>
                      <span className="review-option-text">{option.text}</span>
                      {isChosen && <span className="tag">Your answer</span>}
                      {isCorrectOption && <span className="tag tag-ok">Correct answer</span>}
                    </li>
                  )
                })}
              </ul>

              {item.selectedOptionId == null && (
                <p className="review-note">You left this question unanswered.</p>
              )}

              <p className="explanation">
                <strong>Why:</strong> {item.explanation}
              </p>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}
