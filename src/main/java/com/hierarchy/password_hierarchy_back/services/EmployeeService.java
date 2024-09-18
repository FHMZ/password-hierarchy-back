package com.hierarchy.password_hierarchy_back.services;

import com.hierarchy.password_hierarchy_back.exceptions.EmployeeNotFoundException;
import com.hierarchy.password_hierarchy_back.models.dtos.*;
import com.hierarchy.password_hierarchy_back.models.entities.Employee;
import com.hierarchy.password_hierarchy_back.repos.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hierarchy.password_hierarchy_back.models.dtos.DependentDTO.toDependents;
import static com.hierarchy.password_hierarchy_back.models.dtos.EmployeeResponseDTO.fromEntity;
import static com.hierarchy.password_hierarchy_back.models.dtos.EmployeeResponseDTO.toEmployees;
import static com.hierarchy.password_hierarchy_back.models.entities.Employee.toPersist;
import static com.hierarchy.password_hierarchy_back.utils.PasswordUtils.calculatePasswordStrength;
import static com.hierarchy.password_hierarchy_back.utils.PasswordUtils.encryptPassword;
import static com.hierarchy.password_hierarchy_back.utils.StrengthLabel.getLabelByStrength;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class EmployeeService {

    private final EmployeeRepo employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepo employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void createEmployee(EmployeeRequestDTO employeeRequest) {
        final long passwordStrengthValue = validatePasswordStrength(employeeRequest.getPassword());
        final Long dependentId = employeeRequest.getDependentId();

        final Employee employeeEntity = toPersist(employeeRequest);

        final String encryptedPassword = encryptPassword(employeeRequest.getPassword());

        employeeEntity.setDependentId(dependentId == 0 ? null : dependentId);
        employeeEntity.setPassword(encryptedPassword);
        employeeEntity.setPasswordStrengthValue(passwordStrengthValue);

        employeeRepository.save(employeeEntity);
    }

    public EmployeeResponseDTO getEmployeeById(Long id) {
        final Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + id));
        return fromEntity(employee);
    }

    public List<EmployeeResponseDTO> getAllEmployees() {
        final List<Employee> employees = employeeRepository.findByDependentIdIsNull();
        return employees.stream()
                .map(EmployeeResponseDTO::fromEntity)
                .toList();
    }

    public List<DependentDTO> getDependents(Long id) {
        List<Employee> dependents;

        if (isNull(id) || id == 0) {
            dependents = employeeRepository.findAll();
        } else {
            dependents = employeeRepository.findAllByIdNot(id);
        }

        return toDependents(dependents);
    }

    public List<EmployeeResponseDTO> getByName(String param) {
        final List<Employee> employees = employeeRepository.findByNameContaining(param);
        return toEmployees(employees);
    }

    public PasswordResponseDTO getScore(PasswordRequestDTO itemRequest) {
        final long score = calculatePasswordStrength(itemRequest.getItem());
        final String label = getLabelByStrength(score);
        final String text = format("Nível de senha %s %d%%", label, score);

        return PasswordResponseDTO.builder().value(score).text(text).build();
    }

    public void updateEmployee(Long id, EmployeeRequestDTO employeeRequest) {
        final Employee existingEmployee = findEmployeeById(id);
        final Set<Long> dependentIds = extractIds(existingEmployee.getDependents());
        final boolean isPresent = dependentIds.contains(employeeRequest.getDependentId());

        if (isPresent) {
            throw new IllegalArgumentException("The employee ID is a dependent of the current employee and cannot be assigned.");
        }

        final Employee employeeEntity = toPersist(id, employeeRequest);

        if (nonNull(employeeRequest.getPassword())) {
            final long passwordStrengthValue = validatePasswordStrength(employeeRequest.getPassword());
            final String encryptedPassword = encryptPassword(employeeRequest.getPassword());
            final Long dependentId = employeeRequest.getDependentId();

            employeeEntity.setDependentId(dependentId == 0 ? null : dependentId);
            employeeEntity.setPassword(encryptedPassword);
            employeeEntity.setPasswordStrengthValue(passwordStrengthValue);
        } else {
            throw new IllegalArgumentException("Password field must be present.");
        }

        employeeRepository.save(employeeEntity);
    }

    public void deleteEmployee(Long id) {
        final Employee existingEmployee = findEmployeeById(id);

        if (existingEmployee.getDependents().isEmpty()) {
            employeeRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("The employee has dependents and con not be deleted.");
        }
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + id));
    }

    private long validatePasswordStrength(String password) {
        final long strength = calculatePasswordStrength(password);
        if (strength < 3) {
            throw new IllegalArgumentException("Password strength is too weak.");
        }
        return strength;
    }

    private Set<Long> extractIds(List<Employee> dependents) {
        return dependents.stream().map(Employee::getId).collect(Collectors.toSet());
    }
}
