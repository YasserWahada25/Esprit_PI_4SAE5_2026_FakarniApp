package com.alzheimer.activite_educative_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/** Option QCM côté jeu (sans indicateur de bonne réponse). */
public class QuizOptionPlayDto {

    private String label;

    @JsonAlias({"image_url", "IMAGE_URL"})
    private String imageUrl;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
