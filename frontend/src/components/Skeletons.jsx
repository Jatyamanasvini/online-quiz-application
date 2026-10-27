export function CourseGridSkeleton() {
  return (
    <div className="course-grid" aria-hidden="true">
      {Array.from({ length: 6 }).map((_, index) => (
        <div className="course-card skeleton-card" key={index}>
          <span className="skeleton skeleton-icon" />
          <span className="course-body">
            <span className="skeleton skeleton-line w-60" />
            <span className="skeleton skeleton-line" />
            <span className="skeleton skeleton-line w-40" />
          </span>
        </div>
      ))}
    </div>
  )
}

export function TopicListSkeleton() {
  return (
    <div className="topic-list" aria-hidden="true">
      {Array.from({ length: 5 }).map((_, index) => (
        <div className="topic-row skeleton-card" key={index}>
          <span className="skeleton skeleton-badge" />
          <span className="topic-body">
            <span className="skeleton skeleton-line w-40" />
            <span className="skeleton skeleton-line w-70" />
          </span>
        </div>
      ))}
    </div>
  )
}
