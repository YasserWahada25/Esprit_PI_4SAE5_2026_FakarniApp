package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.dto.QuestionRequest;
import com.alzheimer.activite_educative_service.dto.QuestionResponse;
import com.alzheimer.activite_educative_service.services.EducationalQuestionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/activities/{activityId}/questions", "/activities/{activityId}/questions"})
public class EducationalQuestionController {

    private final EducationalQuestionService questionService;

    public EducationalQuestionController(EducationalQuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionResponse> list(@PathVariable Long activityId) {
        return questionService.listQuestions(activityId);
    }

    @GetMapping("/{questionId}")
    public QuestionResponse get(
            @PathVariable Long activityId,
            @PathVariable Long questionId
    ) {
        return questionService.getQuestion(activityId, questionId);
    }

    @PostMapping
    public QuestionResponse create(
            @PathVariable Long activityId,
            @Valid @RequestBody QuestionRequest request
    ) {
        return questionService.createQuestion(activityId, request);
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public QuestionResponse createMultipart(
            @PathVariable Long activityId,
            @Valid @RequestPart("question") QuestionRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return questionService.createQuestion(activityId, request, image);
    }

    @PutMapping("/{questionId}")
    public QuestionResponse update(
            @PathVariable Long activityId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request
    ) {
        return questionService.updateQuestion(activityId, questionId, request);
    }

    @PutMapping(value = "/{questionId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public QuestionResponse updateMultipart(
            @PathVariable Long activityId,
            @PathVariable Long questionId,
            @Valid @RequestPart("question") QuestionRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return questionService.updateQuestion(activityId, questionId, request, image);
    }

    @DeleteMapping("/{questionId}")
    public void delete(
            @PathVariable Long activityId,
            @PathVariable Long questionId
    ) {
        questionService.deleteQuestion(activityId, questionId);
    }
}
