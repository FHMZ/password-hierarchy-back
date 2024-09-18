package com.hierarchy.password_hierarchy_back.models.dtos;

import com.hierarchy.password_hierarchy_back.models.entities.Employee;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DependentDTO {

    private Long id;
    private String name;

    public static List<DependentDTO> toDependents(List<Employee> employees) {
        return employees.stream().map(DependentDTO::toDTO).toList();
    }

    public static DependentDTO toDTO(Employee employee) {
        return DependentDTO.builder().id(employee.getId()).name(employee.getName()).build();
    }

}
