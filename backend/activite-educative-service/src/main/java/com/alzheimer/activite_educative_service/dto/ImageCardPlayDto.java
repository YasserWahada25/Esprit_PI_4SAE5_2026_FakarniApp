package com.alzheimer.activite_educative_service.dto;

/**
 * Carte exposée au démarrage d’une session MEMORY_MATCH (sans identifiant de paire).
 */
public class ImageCardPlayDto {

    private Long id;
    private String imageUrl;
    /** Texte au dos / accessibilité (ex. nom du thème). */
    private String backLabel;

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

    public String getBackLabel() {
        return backLabel;
    }

    public void setBackLabel(String backLabel) {
        this.backLabel = backLabel;
    }
}
