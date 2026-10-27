import { useState } from 'react'
import { AlertIcon, ArrowLeftIcon, ArrowRightIcon, CloseIcon } from '../components/Icons.jsx'

export default function Quiz({ quiz, onSubmit, submitting, error, onExit }) {
  const questions = quiz.questions
  const total = questions.length

  const [index, setIndex] = useState(0)
  const [answers, setAnswers] = useState({})
  const [confirmSubmit, setConfirmSubmit] = useState(false)
  const [confirmExit, setConfirmExit] = useState(false)

  const current = questions[index]
  const selectedOptionId = answers[current.id]
  const answeredCount = Object.keys(answers).length
  const unanswered = total - answeredCount
  const isLast = index === total - 1

  const selectOption = (optionId) => {
    setAnswers((previous) => ({ ...previous, [current.id]: optionId }))
  }

  const buildPayload = () =>
    questions.map((question) => ({
      questionId: question.id,
      optionId: answers[question.id] ?? null,
    }))

  const handlePrimaryAction = () => {
    if (!isLast) {
      setIndex((value) => Math.min(value + 1, total - 1))
      return
    }
    if (unanswered > 0) {
      setConfirmSubmit(true)
      return
    }
    onSubmit(buildPayload())
  }

  return (
    <main id="main" className="page page-quiz">
      <div className="quiz-bar">
        <button type="button" className="btn ghost small" onClick={() => setConfirmExit(true)}>
          <CloseIcon /> Exit
        </button>
        <span className="quiz-context">
          {quiz.courseTitle} · {quiz.topicTitle}
        </span>
        <span className="quiz-counter">
          {index + 1} / {total}
        </span>
      </div>

      <div
        className="progress"
        role="progressbar"
        aria-label="Quiz progress"
        aria-valuemin={1}
        aria-valuemax={total}
        aria-valuenow={index + 1}
        aria-valuetext={`Question ${index + 1} of ${total}`}
      >
        <span style={{ width: `${((index + 1) / total) * 100}%` }} />
      </div>

      <div className="quiz-meta">
        <span>
          {answeredCount} of {total} answered
        </span>
        <span className={unanswered > 0 ? 'muted' : 'ok-text'}>
          {unanswered > 0 ? `${unanswered} remaining` : 'All answered'}
        </span>
      </div>

      <section className="question-card">
        <fieldset>
          <legend className="question-text">
            <span className="q-index" aria-hidden="true">
              Q{index + 1}
            </span>
            <span>{current.text}</span>
          </legend>

          <div className="options">
            {current.options.map((option, optionIndex) => {
              const isSelected = selectedOptionId === option.id
              return (
                <label
                  key={option.id}
                  className={`option${isSelected ? ' is-selected' : ''}`}
                >
                  <input
                    type="radio"
                    name={`question-${current.id}`}
                    value={option.id}
                    aria-label={`Option ${String.fromCharCode(65 + optionIndex)}: ${option.text}`}
                    checked={isSelected}
                    onChange={() => selectOption(option.id)}
                  />
                  <span className="option-key" aria-hidden="true">
                    {String.fromCharCode(65 + optionIndex)}
                  </span>
                  <span className="option-text">{option.text}</span>
                </label>
              )
            })}
          </div>
        </fieldset>
      </section>

      {error && (
        <p className="inline-error" role="alert">
          <AlertIcon /> {error.message}
        </p>
      )}

      {confirmSubmit && (
        <div className="confirm-panel" role="alertdialog" aria-labelledby="submit-confirm-title">
          <p id="submit-confirm-title">
            {unanswered === 1
              ? 'One question is unanswered.'
              : `${unanswered} questions are unanswered.`}{' '}
            Unanswered questions count as incorrect.
          </p>
          <div className="confirm-actions">
            <button type="button" className="btn ghost" onClick={() => setConfirmSubmit(false)}>
              Keep answering
            </button>
            <button
              type="button"
              className="btn primary"
              onClick={() => onSubmit(buildPayload())}
              disabled={submitting}
            >
              {submitting ? 'Scoring…' : 'Submit anyway'}
            </button>
          </div>
        </div>
      )}

      {confirmExit && (
        <div className="confirm-panel" role="alertdialog" aria-labelledby="exit-confirm-title">
          <p id="exit-confirm-title">Leave this quiz? Your answers will be lost.</p>
          <div className="confirm-actions">
            <button type="button" className="btn ghost" onClick={() => setConfirmExit(false)}>
              Stay
            </button>
            <button type="button" className="btn danger" onClick={onExit}>
              Leave quiz
            </button>
          </div>
        </div>
      )}

      <nav className="quiz-nav" aria-label="Quiz navigation">
        <button
          type="button"
          className="btn ghost"
          onClick={() => setIndex((value) => Math.max(0, value - 1))}
          disabled={index === 0}
        >
          <ArrowLeftIcon /> Previous
        </button>

        <div className="dots" aria-label="Jump to question">
          {questions.map((question, questionIndex) => (
            <button
              key={question.id}
              type="button"
              className={`dot-btn${questionIndex === index ? ' is-current' : ''}${
                answers[question.id] ? ' is-answered' : ''
              }`}
              aria-label={`Question ${questionIndex + 1}${
                answers[question.id] ? ', answered' : ''
              }`}
              aria-current={questionIndex === index ? 'step' : undefined}
              onClick={() => setIndex(questionIndex)}
            />
          ))}
        </div>

        <button
          type="button"
          className="btn primary"
          onClick={handlePrimaryAction}
          disabled={submitting}
        >
          {submitting ? (
            'Scoring…'
          ) : isLast ? (
            'Submit quiz'
          ) : (
            <>
              Next <ArrowRightIcon />
            </>
          )}
        </button>
      </nav>
    </main>
  )
}
