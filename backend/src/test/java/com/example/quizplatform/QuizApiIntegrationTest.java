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
                submitBody(topicId, answers));

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

        HttpResponse<String> response = post("/api/quizzes/submit", submitBody(topicId, wrongAnswers));

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

        HttpResponse<String> response = post("/api/quizzes/submit", submitBody(topicId, partial));

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

        HttpResponse<String> response = post("/api/quizzes/submit", body);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("does not belong to question"));
    }

    @Test
    void rejectsQuestionFromAnotherTopic() throws Exception {
        long javaTopic = topicId("java-basics");
        long pythonTopic = topicId("python-basics");
        long foreignQuestion = questionIds(pythonTopic).get(0);

        HttpResponse<String> response = post("/api/quizzes/submit",
                submitBody(javaTopic, List.of(answer(foreignQuestion, null))));

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("does not belong to topic"));
    }

    @Test
    void rejectsDuplicateAnswersAndMissingTopicId() throws Exception {
        long topicId = topicId("java-basics");
        long questionId = questionIds(topicId).get(0);

        HttpResponse<String> duplicate = post("/api/quizzes/submit",
                submitBody(topicId, List.of(answer(questionId, null), answer(questionId, null))));
        assertEquals(400, duplicate.statusCode());
        assertTrue(duplicate.body().contains("Duplicate answer"));

        HttpResponse<String> missingTopic = post("/api/quizzes/submit", "{\"answers\":[]}");
        assertEquals(400, missingTopic.statusCode());

        HttpResponse<String> malformed = post("/api/quizzes/submit", "{not json");
        assertEquals(400, malformed.statusCode());
    }

    @Test
    void topicWithoutQuestionsReportsConflict() throws Exception {
        long topicId = topicId("dbms-joins");

        HttpResponse<String> quiz = get("/api/topics/" + topicId + "/quiz");
        assertEquals(409, quiz.statusCode());
        assertTrue(quiz.body().contains("No questions"));

        HttpResponse<String> submit = post("/api/quizzes/submit", submitBody(topicId, List.of()));
        assertEquals(409, submit.statusCode());
    }

    @Test
    void unknownTopicReturns404() throws Exception {
        assertEquals(404, get("/api/topics/999999/quiz").statusCode());
        assertEquals(404, post("/api/quizzes/submit", submitBody(999999L, List.of())).statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
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
