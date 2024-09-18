package com.hierarchy.password_hierarchy_back.models.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseDTO {

    private int statusCode;
    private String message;

    public ErrorResponseDTO(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

}
