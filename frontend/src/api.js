const API_BASE = import.meta.env.VITE_API_BASE ?? ''
const TIMEOUT_MS = 15000
const TOKEN_KEY = 'quizora-token'

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function getToken() {
  return window.localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  window.localStorage.removeItem(TOKEN_KEY)
}

async function request(path, { method = 'GET', body } = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS)

  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  try {
    const response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
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
      if (response.status === 401 && token) clearToken()
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

// Catalogue
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

// Auth
export function login(email, password) {
  return request('/api/auth/login', { method: 'POST', body: { email, password } })
}

export function register(name, email, password) {
  return request('/api/auth/register', { method: 'POST', body: { name, email, password } })
}

export function fetchMe() {
  return request('/api/auth/me')
}

// Attempts
export function fetchMyAttempts() {
  return request('/api/attempts/my')
}

export function fetchAttempt(attemptId) {
  return request(`/api/attempts/${attemptId}`)
}

// Teacher
export function fetchTeacherAttempts() {
  return request('/api/teacher/attempts')
}

export function fetchTeacherStats() {
  return request('/api/teacher/stats')
}
