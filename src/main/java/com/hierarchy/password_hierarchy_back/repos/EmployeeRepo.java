package com.hierarchy.password_hierarchy_back.repos;

import com.hierarchy.password_hierarchy_back.models.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {

    List<Employee> findByDependentIdIsNull();

    List<Employee> findAllByIdNot(Long id);

    List<Employee> findByNameContaining(String name);

}
