package com.alzheimer.session_service.services;

import com.alzheimer.session_service.dto.StaticTestUserResponse;
import com.alzheimer.session_service.entities.TestUser;
import com.alzheimer.session_service.repositories.TestUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestUserService {

	public static final Long STATIC_TEST_USER_ID = 1L;
	public static final String STATIC_TEST_USER_NAME = "Utilisateur Test";
	public static final String STATIC_TEST_USER_EMAIL = "test@fakarni.com";
	public static final String STATIC_TEST_USER_ROLE = "Utilisateur";

	private final TestUserRepository testUserRepository;

	public TestUserService(TestUserRepository testUserRepository) {
		this.testUserRepository = testUserRepository;
	}

	@Transactional
	public TestUser ensureStaticTestUser() {
		return testUserRepository.findById(STATIC_TEST_USER_ID).orElseGet(() -> {
			TestUser user = TestUser.builder()
					.id(STATIC_TEST_USER_ID)
					.name(STATIC_TEST_USER_NAME)
					.email(STATIC_TEST_USER_EMAIL)
					.role(STATIC_TEST_USER_ROLE)
					.build();
			return testUserRepository.save(user);
		});
	}

	public StaticTestUserResponse getStaticTestUser() {
		TestUser user = ensureStaticTestUser();
		return StaticTestUserResponse.builder()
				.id(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.role(user.getRole())
				.build();
	}
}
