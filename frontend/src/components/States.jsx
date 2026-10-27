import { AlertIcon, RefreshIcon } from './Icons.jsx'

export function ErrorState({ title = 'Something went wrong', message, onRetry }) {
  return (
    <div className="state-card error-state" role="alert">
      <span className="state-icon" aria-hidden="true">
        <AlertIcon />
      </span>
      <div>
        <h3>{title}</h3>
        <p>{message || 'Please try again in a moment.'}</p>
      </div>
      {onRetry && (
        <button type="button" className="btn ghost" onClick={onRetry}>
          <RefreshIcon /> Retry
        </button>
      )}
    </div>
  )
}

export function EmptyState({ title, message }) {
  return (
    <div className="state-card">
      <div>
        <h3>{title}</h3>
        <p>{message}</p>
      </div>
    </div>
  )
}
