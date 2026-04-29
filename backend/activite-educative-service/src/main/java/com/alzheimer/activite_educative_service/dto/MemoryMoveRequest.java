package com.alzheimer.activite_educative_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public class MemoryMoveRequest {

    @NotNull
    @JsonAlias({"first_card_id", "firstCardId"})
    private Long firstCardId;

    @NotNull
    @JsonAlias({"second_card_id", "secondCardId"})
    private Long secondCardId;

    public Long getFirstCardId() {
        return firstCardId;
    }

    public void setFirstCardId(Long firstCardId) {
        this.firstCardId = firstCardId;
    }

    public Long getSecondCardId() {
        return secondCardId;
    }

    public void setSecondCardId(Long secondCardId) {
        this.secondCardId = secondCardId;
    }
}
