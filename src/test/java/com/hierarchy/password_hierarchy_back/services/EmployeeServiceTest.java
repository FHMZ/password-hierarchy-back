package com.hierarchy.password_hierarchy_back.services;

import com.hierarchy.password_hierarchy_back.exceptions.EmployeeNotFoundException;
import com.hierarchy.password_hierarchy_back.models.dtos.*;
import com.hierarchy.password_hierarchy_back.models.entities.Employee;
import com.hierarchy.password_hierarchy_back.repos.EmployeeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static com.hierarchy.password_hierarchy_back.models.dtos.DependentDTO.toDTO;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(EmployeeService.class)
public class EmployeeServiceTest {

    @MockBean
    private EmployeeRepo employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    private Employee mockEmployee;
    private EmployeeRequestDTO mockEmployeeRequestDTO;
    private List<Employee> mockEmployeeList;
    private List<DependentDTO> mockDependentDTOList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockEmployee = createMockEmployee();
        mockEmployeeRequestDTO = createMockEmployeeRequestDTO();
        mockEmployeeList = singletonList(mockEmployee);
        mockDependentDTOList = singletonList(toDTO((mockEmployee)));
    }

    @Test
    void createEmployee_ShouldSaveEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        employeeService.createEmployee(mockEmployeeRequestDTO);

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(mockEmployee.getName(), result.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_ShouldThrowException_WhenEmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.getEmployeeById(1L);
        });

        String expectedMessage = "Employee not found with id 1";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() {
        List<Employee> employees = singletonList(mockEmployee);
        when(employeeRepository.findByDependentIdIsNull()).thenReturn(employees);

        List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findByDependentIdIsNull();
    }

    @Test
    void getDependents_ShouldReturnListOfDependents_WhenIdIsNotNull() {
        List<Employee> dependents = singletonList(mockEmployee);
        when(employeeRepository.findAllByIdNot(1L)).thenReturn(dependents);

        List<DependentDTO> result = employeeService.getDependents(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findAllByIdNot(1L);
    }

    @Test
    void getDependents_WithZeroId_ShouldReturnAllDependents() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(mockEmployeeList);

        // Act
        List<DependentDTO> result = employeeService.getDependents(0L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockDependentDTOList.get(0).getName(), result.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void getScore_ShouldReturnPasswordScore() {
        PasswordRequestDTO passwordRequest = new PasswordRequestDTO();
        passwordRequest.setItem("strongPassword123!");

        PasswordResponseDTO result = employeeService.getScore(passwordRequest);

        assertNotNull(result);
        assertTrue(result.getValue() > 0);
    }

    @Test
    void getByName_ShouldReturnMatchingEmployees() {
        // Arrange
        String searchParam = "John";
        List<Employee> mockEmployees = Arrays.asList(
                new Employee(1L, "Marie Johnson", "john.doe@example.com", "password1242", 80L, null, new ArrayList<>()),
                new Employee(2L, "John Smith", "john.smith@example.com", "12345password", 70L, null, new ArrayList<>())
        );

        when(employeeRepository.findByNameContaining(searchParam)).thenReturn(mockEmployees);

        List<EmployeeResponseDTO> result = employeeService.getByName(searchParam);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Marie Johnson", result.get(0).getName());
        assertEquals("John Smith", result.get(1).getName());
        verify(employeeRepository, times(1)).findByNameContaining(searchParam);
    }

    @Test
    void updateEmployee_ShouldUpdateEmployee_WhenEmployeeIsFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        employeeService.updateEmployee(1L, mockEmployeeRequestDTO);

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_ShouldThrowException_WhenPasswordIsMissing() {
        mockEmployeeRequestDTO.setPassword(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployee(1L, mockEmployeeRequestDTO);
        });

        String expectedMessage = "Password field must be present.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void deleteEmployee_ShouldDeleteEmployee_WhenNoDependents() {
        mockEmployee.setDependents(Collections.emptyList());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEmployee_ShouldThrowException_WhenEmployeeHasDependents() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(1L);
        });

        String expectedMessage = "The employee has dependents and con not be deleted.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.equals(expectedMessage));
    }

    private Employee createMockEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setPasswordStrengthValue(70L);
        employee.setPassword("password123");

        Employee employee1 = new Employee();
        employee1.setId(2L);
        employee1.setName("Marie Key");
        employee1.setPasswordStrengthValue(50L);
        employee1.setPassword("password1287867");

        Employee employee2 = new Employee();
        employee2.setId(1L);
        employee2.setName("Peter Elison");
        employee2.setPasswordStrengthValue(60L);
        employee2.setPassword("password14321");

        List<Employee> dependents = new ArrayList<>();
        dependents.add(employee1);
        dependents.add(employee2);

        employee.setDependents(dependents);

        return employee;
    }

    private EmployeeRequestDTO createMockEmployeeRequestDTO() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("John Doe");
        dto.setPassword("password123");
        dto.setDependentId(0L);
        return dto;
    }

    private EmployeeResponseDTO createMockEmployeeResponseDTO() {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setPasswordStrengthValue(80L);
        return dto;
    }
}
