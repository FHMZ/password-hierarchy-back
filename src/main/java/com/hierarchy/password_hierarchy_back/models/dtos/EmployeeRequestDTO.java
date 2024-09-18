package com.hierarchy.password_hierarchy_back.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import lombok.*;

@Setter
@Getter
@Builder
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Long dependentId;

}
