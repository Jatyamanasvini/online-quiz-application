import { ArrowRightIcon } from './Icons.jsx'

export default function CourseCard({ course, index, onSelect }) {
  const accent = course.accent || 'var(--accent)'

  return (
    <button
      type="button"
      className="course-card"
      style={{ '--card-accent': accent, animationDelay: `${Math.min(index, 9) * 45}ms` }}
      onClick={() => onSelect(course)}
      aria-label={`Open ${course.title}, ${course.topicCount} topics`}
    >
      <span className="course-icon" aria-hidden="true">
        {course.icon}
      </span>

      <span className="course-body">
        <span className="course-title">{course.title}</span>
        <span className="course-desc">{course.description}</span>
        <span className="course-meta">
          <span>{course.topicCount} topics</span>
          <span className="dot" aria-hidden="true" />
          <span>
            {course.questionCount === 0 ? 'Coming soon' : `${course.questionCount} questions`}
          </span>
        </span>
      </span>

      <span className="course-arrow" aria-hidden="true">
        <ArrowRightIcon />
      </span>
    </button>
  )
}
