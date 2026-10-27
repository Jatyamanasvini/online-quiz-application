const API_BASE = import.meta.env.VITE_API_BASE ?? ''
const TIMEOUT_MS = 15000

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request(path, { method = 'GET', body } = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS)

  try {
    const response = await fetch(`${API_BASE}${path}`, {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: controller.signal,
    })

    const text = await response.text()
    let payload = null
    if (text) {
      try {
        payload = JSON.parse(text)
      } catch {
        payload = null
      }
    }

    if (!response.ok) {
      const message =
        payload?.message || `Request failed with status ${response.status}`
      throw new ApiError(message, response.status)
    }

    return payload
  } catch (error) {
    if (error instanceof ApiError) throw error
    if (error.name === 'AbortError') {
      throw new ApiError('The request timed out. Is the backend still running?')
    }
    throw new ApiError(
      'Could not reach the quiz server. Make sure the Spring Boot backend is running on port 8080.',
    )
  } finally {
    clearTimeout(timer)
  }
}

export function fetchCourses() {
  return request('/api/courses')
}

export function fetchTopics(courseId) {
  return request(`/api/courses/${courseId}/topics`)
}

export function fetchQuiz(topicId) {
  return request(`/api/topics/${topicId}/quiz`)
}

export function submitQuiz(topicId, answers) {
  return request('/api/quizzes/submit', {
    method: 'POST',
    body: { topicId, answers },
  })
}
