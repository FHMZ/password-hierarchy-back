package com.hierarchy.password_hierarchy_back.models.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResponseDTO {

    private long value;
    private String text;

}
