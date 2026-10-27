package com.example.quizplatform;

import com.example.quizplatform.repository.CourseRepository;
import com.example.quizplatform.repository.QuestionRepository;
import com.example.quizplatform.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class QuizApiIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void listsAllSeededCoursesWithCounts() throws Exception {
        HttpResponse<String> response = get("/api/courses");

        assertEquals(200, response.statusCode());
        JsonNode body = objectMapper.readTree(response.body());
        assertEquals(10, body.size());

        JsonNode java = body.get(0);
        assertEquals("java", java.get("code").asText());
        assertEquals("Java Programming", java.get("title").asText());
        assertEquals(6, java.get("topicCount").asLong());
        assertEquals(24, java.get("questionCount").asLong());

        JsonNode aptitude = body.get(9);
        assertEquals("aptitude", aptitude.get("code").asText());
        assertEquals(4, aptitude.get("topicCount").asLong());
        assertEquals(0, aptitude.get("questionCount").asLong());
    }

    @Test
    void listsTopicsForCourseInOrder() throws Exception {
        long javaId = courseId("java");

        HttpResponse<String> response = get("/api/courses/" + javaId + "/topics");

        assertEquals(200, response.statusCode());
        JsonNode topics = objectMapper.readTree(response.body());
        assertEquals(6, topics.size());
        assertEquals("java-basics", topics.get(0).get("slug").asText());
        assertEquals("Basics", topics.get(0).get("title").asText());
        assertEquals(4, topics.get(0).get("questionCount").asLong());
        assertEquals("java-multithreading", topics.get(5).get("slug").asText());
    }

    @Test
    void unknownCourseReturns404() throws Exception {
        HttpResponse<String> response = get("/api/courses/999999/topics");

        assertEquals(404, response.statusCode());
        JsonNode error = objectMapper.readTree(response.body());
        assertEquals(404, error.get("status").asInt());
        assertTrue(error.get("message").asText().contains("999999"));
    }

    @Test
    void quizPayloadNeverExposesAnswerKeys() throws Exception {
        long topicId = topicId("java-basics");

        HttpResponse<String> response = get("/api/topics/" + topicId + "/quiz");

        assertEquals(200, response.statusCode());
        JsonNode quiz = objectMapper.readTree(response.body());
        assertEquals(4, quiz.get("questionCount").asInt());
        assertEquals("Basics", quiz.get("topicTitle").asText());

        for (JsonNode question : quiz.get("questions")) {
            assertFalse(question.has("correct"), "question must not expose a correctness flag");
            assertFalse(question.has("explanation"), "question must not expose an explanation");
            assertFalse(question.has("correctOptionId"), "question must not expose the answer");
            for (JsonNode option : question.get("options")) {
                assertTrue(option.has("id"));
                assertTrue(option.has("text"));
                assertFalse(option.has("correct"));
                assertEquals(2, option.size(), "options should only contain id and text");
            }
        }
    }

    @Test
    void submittingCorrectAnswersScoresFullMarks() throws Exception {
        long topicId = topicId("java-basics");
        List<Map<String, Object>> answers = correctAnswers(topicId);

        HttpResponse<String> response = post("/api/quizzes/submit",
                submitBody(topicId, answers), studentToken());

        assertEquals(200, response.statusCode());
        JsonNode result = objectMapper.readTree(response.body());
        assertEquals(4, result.get("total").asInt());
        assertEquals(4, result.get("correct").asInt());
        assertEquals(0, result.get("incorrect").asInt());
        assertEquals(0, result.get("unanswered").asInt());
        assertEquals(100.0, result.get("accuracy").asDouble());

        for (JsonNode review : result.get("results")) {
            assertTrue(review.get("correct").asBoolean());
            assertTrue(review.has("correctOptionId"));
            assertTrue(review.has("explanation"));
            assertEquals(review.get("correctOptionId").asLong(),
                    review.get("selectedOptionId").asLong());
        }
    }

    @Test
    void submittingWrongAnswersIsScoredAsIncorrect() throws Exception {
        long topicId = topicId("python-basics");
        Map<Long, Long> correct = optionMap(questionRepository.findCorrectOptions(topicId));
        List<Map<String, Object>> wrongAnswers = new ArrayList<>();
        for (Long questionId : correct.keySet()) {
            wrongAnswers.add(answer(questionId, firstIncorrectOption(topicId, questionId)));
        }

        HttpResponse<String> response = post("/api/quizzes/submit", submitBody(topicId, wrongAnswers), studentToken());

        assertEquals(200, response.statusCode());
        JsonNode result = objectMapper.readTree(response.body());
        assertEquals(4, result.get("total").asInt());
        assertEquals(0, result.get("correct").asInt());
        assertEquals(4, result.get("incorrect").asInt());
        assertEquals(0.0, result.get("accuracy").asDouble());
        assertTrue(result.get("results").get(0).get("correctOptionId").asLong() > 0);
    }

    @Test
    void unansweredQuestionsCountAsIncorrectButNotUnansweredFlagMismatch() throws Exception {
        long topicId = topicId("web-html");
        List<Map<String, Object>> partial = new ArrayList<>();
        partial.add(answer(questionIdAt(topicId, 0), correctOptionFor(topicId, 0)));

        HttpResponse<String> response = post("/api/quizzes/submit", submitBody(topicId, partial), studentToken());

        assertEquals(200, response.statusCode());
        JsonNode result = objectMapper.readTree(response.body());
        assertEquals(4, result.get("total").asInt());
        assertEquals(1, result.get("correct").asInt());
        assertEquals(3, result.get("incorrect").asInt());
        assertEquals(3, result.get("unanswered").asInt());
        assertEquals(25.0, result.get("accuracy").asDouble());
    }

    @Test
    void rejectsOptionThatBelongsToAnotherQuestion() throws Exception {
        long topicId = topicId("java-basics");
        List<Long> questionIds = questionIds(topicId);
        Map<Long, Long> correct = optionMap(questionRepository.findCorrectOptions(topicId));

        long optionOfAnotherQuestion = correct.get(questionIds.get(1));
        String body = submitBody(topicId, List.of(answer(questionIds.get(0), optionOfAnotherQuestion)));

        HttpResponse<String> response = post("/api/quizzes/submit", body, studentToken());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("does not belong to question"));
    }

    @Test
    void rejectsQuestionFromAnotherTopic() throws Exception {
        long javaTopic = topicId("java-basics");
        long pythonTopic = topicId("python-basics");
        long foreignQuestion = questionIds(pythonTopic).get(0);

        HttpResponse<String> response = post("/api/quizzes/submit",
                submitBody(javaTopic, List.of(answer(foreignQuestion, null))), studentToken());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("does not belong to topic"));
    }

    @Test
    void rejectsDuplicateAnswersAndMissingTopicId() throws Exception {
        long topicId = topicId("java-basics");
        long questionId = questionIds(topicId).get(0);

        HttpResponse<String> duplicate = post("/api/quizzes/submit",
                submitBody(topicId, List.of(answer(questionId, null), answer(questionId, null))), studentToken());
        assertEquals(400, duplicate.statusCode());
        assertTrue(duplicate.body().contains("Duplicate answer"));

        HttpResponse<String> missingTopic = post("/api/quizzes/submit", "{\"answers\":[]}", studentToken());
        assertEquals(400, missingTopic.statusCode());

        HttpResponse<String> malformed = post("/api/quizzes/submit", "{not json", studentToken());
        assertEquals(400, malformed.statusCode());
    }

    @Test
    void topicWithoutQuestionsReportsConflict() throws Exception {
        long topicId = topicId("dbms-joins");

        HttpResponse<String> quiz = get("/api/topics/" + topicId + "/quiz");
        assertEquals(409, quiz.statusCode());
        assertTrue(quiz.body().contains("No questions"));

        HttpResponse<String> submit = post("/api/quizzes/submit", submitBody(topicId, List.of()), studentToken());
        assertEquals(409, submit.statusCode());
    }

    @Test
    void unknownTopicReturns404() throws Exception {
        assertEquals(404, get("/api/topics/999999/quiz").statusCode());
        assertEquals(404, post("/api/quizzes/submit", submitBody(999999L, List.of()), studentToken()).statusCode());
    }

    @Test
    void requiresAuthenticationToSubmit() throws Exception {
        long topicId = topicId("java-basics");
        HttpResponse<String> response = post("/api/quizzes/submit", submitBody(topicId, List.of()));

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("Authentication is required"));
    }

    @Test
    void corsAllowsOnlyConfiguredOrigins() throws Exception {
        HttpResponse<String> allowed = preflight("http://localhost:5173");
        assertEquals(200, allowed.statusCode());
        assertEquals("http://localhost:5173",
                allowed.headers().firstValue("Access-Control-Allow-Origin").orElse(null));

        HttpResponse<String> rejected = preflight("https://evil.example");
        assertEquals(403, rejected.statusCode());
        assertTrue(rejected.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    private HttpResponse<String> preflight(String origin) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/api/auth/login"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void registerLoginAndMeFlowWorks() throws Exception {
        HttpResponse<String> registered = post("/api/auth/register",
                "{\"name\":\"Flow User\",\"email\":\"flow@test.local\",\"password\":\"secret123\"}");
        assertEquals(200, registered.statusCode());
        JsonNode auth = objectMapper.readTree(registered.body());
        assertTrue(auth.get("token").asText().length() > 20);
        assertEquals("STUDENT", auth.get("user").get("role").asText());
        assertEquals("flow@test.local", auth.get("user").get("email").asText());

        HttpResponse<String> login = post("/api/auth/login",
                "{\"email\":\"flow@test.local\",\"password\":\"secret123\"}");
        assertEquals(200, login.statusCode());

        String token = auth.get("token").asText();
        HttpResponse<String> me = get("/api/auth/me", token);
        assertEquals(200, me.statusCode());
        assertEquals("Flow User", objectMapper.readTree(me.body()).get("name").asText());

        assertEquals(409, post("/api/auth/register",
                "{\"name\":\"Again\",\"email\":\"flow@test.local\",\"password\":\"secret123\"}").statusCode());

        assertEquals(401, post("/api/auth/login",
                "{\"email\":\"flow@test.local\",\"password\":\"wrong-pass\"}").statusCode());
    }

    @Test
    void submissionIsPersistedForStudentAndTeacher() throws Exception {
        long topicId = topicId("java-basics");
        HttpResponse<String> submitted = post("/api/quizzes/submit",
                submitBody(topicId, correctAnswers(topicId)), studentToken());
        assertEquals(200, submitted.statusCode());

        HttpResponse<String> mine = get("/api/attempts/my", studentToken());
        assertEquals(200, mine.statusCode());
        JsonNode attempts = objectMapper.readTree(mine.body());
        assertTrue(attempts.size() >= 1, "student should see at least one attempt");
        boolean foundBasics = false;
        for (JsonNode attempt : attempts) {
            if ("Basics".equals(attempt.get("topicTitle").asText())) {
                foundBasics = true;
            }
        }
        assertTrue(foundBasics, "attempt list should include the submitted topic");

        String teacher = teacherToken();
        HttpResponse<String> all = get("/api/teacher/attempts", teacher);
        assertEquals(200, all.statusCode());
        assertTrue(all.body().contains("IT Student"), "teacher view should include the student name");

        HttpResponse<String> stats = get("/api/teacher/stats", teacher);
        assertEquals(200, stats.statusCode());
        assertTrue(stats.body().contains("Basics"));

        assertEquals(403, get("/api/teacher/stats", studentToken()).statusCode());
        assertEquals(403, get("/api/teacher/attempts", studentToken()).statusCode());
        assertEquals(401, get("/api/teacher/attempts").statusCode());
        assertEquals(401, get("/api/attempts/my").statusCode());
    }

    @Test
    void studentsCannotReadEachOthersAttempts() throws Exception {
        long topicId = topicId("python-basics");
        assertEquals(200, post("/api/quizzes/submit",
                submitBody(topicId, correctAnswers(topicId)), studentToken()).statusCode());

        JsonNode attempts = objectMapper.readTree(get("/api/attempts/my", studentToken()).body());
        long attemptId = attempts.get(0).get("id").asLong();

        assertEquals(200, get("/api/attempts/" + attemptId, studentToken()).statusCode());

        HttpResponse<String> other = post("/api/auth/register",
                "{\"name\":\"Other User\",\"email\":\"other@test.local\",\"password\":\"secret123\"}");
        assertEquals(200, other.statusCode());
        String otherToken = objectMapper.readTree(other.body()).get("token").asText();

        assertEquals(403, get("/api/attempts/" + attemptId, otherToken).statusCode());
        assertEquals(200, get("/api/attempts/" + attemptId, teacherToken()).statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return get(path, null);
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        return post(path, json, null);
    }

    private HttpResponse<String> post(String path, String json, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String studentToken;
    private static String teacherToken;

    private String studentToken() throws Exception {
        if (studentToken == null) {
            HttpResponse<String> response = post("/api/auth/register",
                    "{\"name\":\"IT Student\",\"email\":\"it.student@test.local\",\"password\":\"secret123\"}");
            if (response.statusCode() != 200) {
                // Already registered by an earlier test method in this context — log in instead.
                response = post("/api/auth/login",
                        "{\"email\":\"it.student@test.local\",\"password\":\"secret123\"}");
            }
            assertEquals(200, response.statusCode(), "student authentication must succeed");
            studentToken = objectMapper.readTree(response.body()).get("token").asText();
        }
        return studentToken;
    }

    private String teacherToken() throws Exception {
        if (teacherToken == null) {
            HttpResponse<String> response = post("/api/auth/login",
                    "{\"email\":\"teacher@quizora.local\",\"password\":\"teacher123\"}");
            assertEquals(200, response.statusCode(), "teacher login must succeed");
            teacherToken = objectMapper.readTree(response.body()).get("token").asText();
        }
        return teacherToken;
    }

    private long courseId(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Missing seeded course " + code))
                .getId();
    }

    private long topicId(String slug) {
        return topicRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalStateException("Missing seeded topic " + slug))
                .getId();
    }

    private List<Long> questionIds(long topicId) {
        return questionRepository.findByTopicIdOrderByPositionAsc(topicId).stream()
                .map(question -> question.getId())
                .toList();
    }

    private long questionIdAt(long topicId, int index) {
        return questionIds(topicId).get(index);
    }

    private Map<Long, Long> optionMap(List<QuestionRepository.QuestionOption> rows) {
        Map<Long, Long> map = new LinkedHashMap<>();
        rows.forEach(row -> map.putIfAbsent(row.getQuestionId(), row.getOptionId()));
        return map;
    }

    private long correctOptionFor(long topicId, int questionIndex) {
        return optionMap(questionRepository.findCorrectOptions(topicId))
                .get(questionIdAt(topicId, questionIndex));
    }

    private long firstIncorrectOption(long topicId, long questionId) {
        return questionRepository.findIncorrectOptions(topicId).stream()
                .filter(row -> row.getQuestionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No incorrect option for " + questionId))
                .getOptionId();
    }

    private List<Map<String, Object>> correctAnswers(long topicId) {
        Map<Long, Long> correct = optionMap(questionRepository.findCorrectOptions(topicId));
        List<Map<String, Object>> answers = new ArrayList<>();
        correct.forEach((questionId, optionId) -> answers.add(answer(questionId, optionId)));
        return answers;
    }

    private Map<String, Object> answer(long questionId, Long optionId) {
        Map<String, Object> answer = new HashMap<>();
        answer.put("questionId", questionId);
        answer.put("optionId", optionId);
        return answer;
    }

    private String submitBody(long topicId, List<Map<String, Object>> answers) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("topicId", topicId);
        body.put("answers", answers);
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void quizViewIncludesCourseContext() throws Exception {
        long topicId = topicId("web-react");
        JsonNode quiz = objectMapper.readTree(get("/api/topics/" + topicId + "/quiz").body());

        assertEquals("React", quiz.get("topicTitle").asText());
        assertEquals("Web Development", quiz.get("courseTitle").asText());
        assertNotNull(quiz.get("courseId"));
    }
}
