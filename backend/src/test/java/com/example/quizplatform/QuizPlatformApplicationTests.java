package com.example.quizplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Keep tests on the in-memory database. Without this the context test starts with the
// default profile, opens (and seeds) the developer's file-based H2 database at
// backend/data/quizdb, and can fail when the app is already running and holds the lock.
@SpringBootTest
@ActiveProfiles("test")
class QuizPlatformApplicationTests {

	@Test
	void contextLoads() {
	}

}
