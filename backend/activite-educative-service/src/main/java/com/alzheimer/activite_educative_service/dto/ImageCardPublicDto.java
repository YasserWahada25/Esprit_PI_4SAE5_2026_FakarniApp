package com.alzheimer.activite_educative_service.dto;

/** Carte pour GET /activities/{id}/image-cards (hors session). */
public class ImageCardPublicDto {

    private Long id;
    private String imageUrl;
    private String label;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
