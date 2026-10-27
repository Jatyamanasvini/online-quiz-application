# Quizora — Online Quiz Application (Problem 7)

Course-based quiz platform: **Spring Boot REST API** + **React (Vite)** frontend.
Courses contain topics, topics contain multiple-choice questions, and the server is the
single source of truth for scoring. Correct answers and explanations are never sent
before submission.

## Stack

| Layer | Choice |
|---|---|
| Backend | Java 17, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Hibernate, Bean Validation |
| Auth | Spring Security + JWT bearer tokens (jjwt 0.12.6, HS384), BCrypt password hashes |
| Database | H2 (file mode) by default — MySQL 8+ profile included |
| Frontend | React 19 + Vite 8, hand-written CSS (design tokens, dark/light) |
| Builds | Maven Wrapper (`./mvnw`) and npm |

## Roles and demo accounts

Two roles are stored in the database (`app_users.role`):

| Role | Can do |
|---|---|
| `STUDENT` | Browse the catalogue, take quizzes, see own attempt history and reviews |
| `TEACHER` | Everything a student can do, plus the class dashboard: every attempt and per-topic averages, with drill-down into any attempt |

Registration through the UI always creates a `STUDENT`. The teacher account is seeded.

| Email | Password | Role |
|---|---|---|
| `teacher@quizora.local` | `teacher123` | TEACHER |
| `student@quizora.local` | `student123` | STUDENT |

These are local demo credentials. Passwords are stored as BCrypt hashes, never in plain text.

## Run it

### 1. Backend (port 8080)

```bash
cd backend
./mvnw spring-boot:run
```

The default profile uses a file-based H2 database at `backend/data/quizdb.mv.db` and seeds
itself on first start. No database server or credentials are required.

### 2. Frontend (port 5173)

```bash
export PATH="$HOME/.local/bin:$PATH"   # user-local Node 24 install on this machine
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. Vite proxies `/api` to `http://localhost:8080`.

### Deploy

Production target: React on **Vercel**, Spring Boot (Docker) on **Render**, MySQL on **Aiven**.
Step-by-step instructions, environment variables and a verification checklist live in
[`DEPLOY.md`](DEPLOY.md); the deploy configs are [`render.yaml`](render.yaml),
[`backend/Dockerfile`](backend/Dockerfile) and [`frontend/vercel.json`](frontend/vercel.json).

### Switch to MySQL

MySQL is **optional**. The default profile runs on H2 and needs no database server or
password, so you can skip this section entirely for local use. MySQL is only needed when you
want to rehearse the production setup.

There is no built-in/default MySQL password, and an existing `root` password cannot be
recovered — only reset. Use whatever admin login you already have (for example a password
manager entry or the account you created when installing MySQL), then create the app user:

```bash
mysql -u root -p -e "CREATE DATABASE quizdb CHARACTER SET utf8mb4;
  CREATE USER 'quizapp'@'localhost' IDENTIFIED BY 'choose-a-password';
  GRANT ALL PRIVILEGES ON quizdb.* TO 'quizapp'@'localhost'; FLUSH PRIVILEGES;"
```

```bash
cd backend
DB_USERNAME=quizapp DB_PASSWORD='choose-a-password' \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Schema is created by Hibernate (`ddl-auto=update`) and the same seeder runs when the
database is empty. Credentials live in environment variables only.

## API

Catalogue reads are public. Submitting a quiz, attempt history and the teacher dashboard require
`Authorization: Bearer <token>`.

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | Create a student account, returns JWT |
| POST | `/api/auth/login` | public | Authenticate, returns JWT + user |
| GET | `/api/auth/me` | authenticated | Current user from the bearer token |
| GET | `/api/courses` | public | Courses with topic and question counts |
| GET | `/api/courses/{courseId}/topics` | public | Topics for a course, with question counts |
| GET | `/api/topics/{topicId}/quiz` | public | Questions + options, **no answer keys** |
| POST | `/api/quizzes/submit` | authenticated | Validate answers, score, persist attempt, return review |
| GET | `/api/attempts/my` | authenticated | Caller's attempts, newest first |
| GET | `/api/attempts/{attemptId}` | owner or TEACHER | Saved review for one attempt |
| GET | `/api/teacher/attempts` | TEACHER | Every student attempt |
| GET | `/api/teacher/stats` | TEACHER | Per-topic attempts, students, average accuracy |

`POST /api/quizzes/submit`

```json
{ "topicId": 1, "answers": [{ "questionId": 1, "optionId": 3 }] }
```

Response contains `total`, `correct`, `incorrect`, `unanswered`, `accuracy`, and per-question
`results` with `selectedOptionId`, `correctOptionId`, `correct`, and `explanation`. The attempt is
persisted for the authenticated user, so the same review is replayable later.

Validation: unknown topic → `404`; question from another topic, option from another
question, duplicate answers or malformed JSON → `400`; topic with no questions or duplicate
registration → `409`; missing/expired token → `401`; wrong role or another student's attempt →
`403`. Errors share `{ timestamp, status, error, message, path }`.

## Auth details

- Tokens are signed with HS384 and expire after 8 hours (`app.jwt.expiry-minutes`).
- The signing secret comes from `JWT_SECRET`; the built-in default is for local development only.
  Set your own before deploying:

```bash
JWT_SECRET='a-long-random-production-secret-at-least-48-bytes' ./mvnw spring-boot:run
```

- The API is stateless (no session cookie), and CSRF is disabled because every mutating request
  carries the bearer token.
- Authorization is enforced on the server per route; the frontend only hides UI it knows the
  role cannot use.
- A separately hosted frontend is allowed through CORS: `CORS_ALLOWED_ORIGINS` is a
  comma-separated list (default `http://localhost:5173`), and it must include the Vercel URL in
  production.
- Demo account passwords are seeded from `SEED_TEACHER_PASSWORD` / `SEED_STUDENT_PASSWORD`
  (defaults `teacher123` / `student123`) and only when the user table is empty.

## Seeded content

- 10 courses, 44 topics.
- 64 questions (4 each) for **Java Programming** (6 topics), **Python Programming**
  (5), and **Web Development** (5). The other seven courses are seeded with topics only
  and show a "Coming soon" empty state instead of fake questions.

## Layout

```
backend/
  src/main/java/com/example/quizplatform/
    domain/       Course, Topic, Question, AnswerOption, AppUser, Role, QuizAttempt, AttemptAnswer
    repository/   Spring Data repositories + count/aggregate projections
    dto/          Request/response records (no entity or hash leakage)
    service/      QuizService (scoring), AttemptService (history/dashboard), AuthService
    security/     SecurityConfig, JwtService, JwtAuthFilter, JSON 401/403 handlers
    web/          REST controllers + ApiExceptionHandler
    config/       DataSeeder (catalogue), UserSeeder (demo accounts)
  src/test/java/…/QuizApiIntegrationTest.java
frontend/
  src/api.js       fetch wrapper, token storage, typed errors
  src/auth.jsx     AuthProvider (session restore, login, register, logout)
  src/auth-context.js  useAuth hook
  src/hooks.js     useAsync (loading/error/retry), useTheme
  src/views/       Home, Topics, TopicIntro, Quiz, Result, Login, MyAttempts, Dashboard
  src/styles.css   tokens, cards, quiz player, auth, tables, responsive rules
```

## Checks

```bash
cd backend && ./mvnw test        # 19 tests (integration + context)
cd frontend && npm run build && npm run lint
```

## Deliberately out of scope

Question editing for teachers, timers, and external AI services. The backend stays the
only authority on correctness.
