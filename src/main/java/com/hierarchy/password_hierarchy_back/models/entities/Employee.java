package com.hierarchy.password_hierarchy_back.models.entities;

import com.hierarchy.password_hierarchy_back.models.dtos.EmployeeRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    private Long passwordStrengthValue;

    @Column(name = "dependent_id")
    private Long dependentId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "dependent_id", insertable = false)
    private List<Employee> dependents;

    public static Employee toPersist(long id, EmployeeRequestDTO request) {
        return Employee.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .dependentId(request.getDependentId())
                .build();
    }

    public static Employee toPersist(EmployeeRequestDTO request) {
        return Employee.builder()
                .id(request.getId())
                .name(request.getName())
                .email(request.getEmail())
                .dependentId(request.getDependentId())
                .build();
    }

}
