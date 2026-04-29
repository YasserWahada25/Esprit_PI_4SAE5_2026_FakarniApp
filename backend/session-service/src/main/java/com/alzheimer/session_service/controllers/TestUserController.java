package com.alzheimer.session_service.controllers;

import com.alzheimer.session_service.dto.StaticTestUserResponse;
import com.alzheimer.session_service.services.TestUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class TestUserController {

	private final TestUserService testUserService;

	public TestUserController(TestUserService testUserService) {
		this.testUserService = testUserService;
	}

	/**
	 * Utilisateur de test figé (dev / intégration). Exposé sans JWT pour faciliter les tests locaux.
	 */
	@GetMapping("/static-test-user")
	public StaticTestUserResponse getStaticTestUser() {
		return testUserService.getStaticTestUser();
	}
}
