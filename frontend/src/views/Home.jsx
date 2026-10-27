import { fetchCourses } from '../api.js'
import { useAsync } from '../hooks.js'
import CourseCard from '../components/CourseCard.jsx'
import { CourseGridSkeleton } from '../components/Skeletons.jsx'
import { EmptyState, ErrorState } from '../components/States.jsx'

export default function Home({ onSelectCourse }) {
  const { status, data, error, retry } = useAsync(fetchCourses, [])
  const courses = data ?? []
  const totals = courses.reduce(
    (sum, course) => ({
      topics: sum.topics + course.topicCount,
      questions: sum.questions + course.questionCount,
    }),
    { topics: 0, questions: 0 },
  )

  return (
    <main id="main" className="page">
      <section className="hero">
        <p className="eyebrow">
          <span className="pulse" aria-hidden="true" />
          Java Full Stack practical · Problem 7
        </p>
        <h1>
          Pick a course.
          <br />
          <span className="gradient-text">Prove it topic by topic.</span>
        </h1>
        <p className="lede">
          Short, focused multiple-choice quizzes graded on the server. Answer at your own
          pace, submit once, then review every explanation.
        </p>

        {status === 'success' && courses.length > 0 && (
          <dl className="hero-stats">
            <div>
              <dt>Courses</dt>
              <dd>{courses.length}</dd>
            </div>
            <div>
              <dt>Topics</dt>
              <dd>{totals.topics}</dd>
            </div>
            <div>
              <dt>Questions</dt>
              <dd>{totals.questions}</dd>
            </div>
          </dl>
        )}
      </section>

      <section className="section" aria-labelledby="courses-heading">
        <div className="section-head">
          <h2 id="courses-heading">Courses</h2>
          {status === 'success' && <span className="muted">{courses.length} available</span>}
        </div>

        {status === 'loading' && <CourseGridSkeleton />}

        {status === 'error' && (
          <ErrorState
            title="Could not load courses"
            message={error?.message}
            onRetry={retry}
          />
        )}

        {status === 'success' && courses.length === 0 && (
          <EmptyState
            title="No courses yet"
            message="Start the backend so the seeded catalogue appears here."
          />
        )}

        {status === 'success' && courses.length > 0 && (
          <div className="course-grid">
            {courses.map((course, index) => (
              <CourseCard
                key={course.id}
                course={course}
                index={index}
                onSelect={onSelectCourse}
              />
            ))}
          </div>
        )}
      </section>
    </main>
  )
}
