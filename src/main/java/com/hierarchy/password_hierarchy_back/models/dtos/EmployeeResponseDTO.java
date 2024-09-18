package com.hierarchy.password_hierarchy_back.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hierarchy.password_hierarchy_back.models.entities.Employee;
import lombok.*;

import java.util.List;

import static com.hierarchy.password_hierarchy_back.utils.StrengthLabel.getLabelByStrength;
import static java.util.Collections.emptyList;

@Setter
@Getter
@Builder
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String passwordStrengthLabel;
    private Long passwordStrengthValue;
    private Long dependentId;
    private List<EmployeeResponseDTO> dependents;

    public static List<EmployeeResponseDTO> toEmployees(List<Employee> employees) {
        return employees.stream().map(EmployeeResponseDTO::fromEntity).toList();
    }

    public static EmployeeResponseDTO fromEntity(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .passwordStrengthLabel(getLabelByStrength(employee.getPasswordStrengthValue()))
                .passwordStrengthValue(employee.getPasswordStrengthValue())
                .dependentId(employee.getDependentId() == null ? 0 : employee.getDependentId())
                .dependents(employee.getDependents() == null ?
                        emptyList() :
                        employee.getDependents().stream()
                                .map(EmployeeResponseDTO::fromEntity)
                                .toList())
                .build();
    }

}
