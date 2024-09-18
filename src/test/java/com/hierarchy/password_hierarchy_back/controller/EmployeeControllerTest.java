package com.hierarchy.password_hierarchy_back.controller;

import com.hierarchy.password_hierarchy_back.controllers.EmployeeController;
import com.hierarchy.password_hierarchy_back.models.dtos.*;
import com.hierarchy.password_hierarchy_back.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    private static final Long EMPLOYEE_ID = 1L;
    private static final String EMPLOYEE_NAME = "John Doe";
    private static final String EMPLOYEE_EMAIL = "john.doe@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponseDTO employeeResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeResponseDTO = createMockEmployeeDTO();
    }

    @Test
    @WithMockUser
    void testCreateEmployee() throws Exception {
        doNothing().when(employeeService).createEmployee(any(EmployeeRequestDTO.class));

        mockMvc.perform(post("/api/employee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\", \"dependentId\": 1 }"))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser
    void testGetEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(EMPLOYEE_ID)).thenReturn(employeeResponseDTO);

        mockMvc.perform(get("/api/employee/{id}", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(EMPLOYEE_NAME))
                .andExpect(jsonPath("$.email").value(EMPLOYEE_EMAIL));

        verify(employeeService, times(1)).getEmployeeById(EMPLOYEE_ID);
    }

    @Test
    @WithMockUser
    void testGetAllEmployees() throws Exception {
        EmployeeResponseDTO employeeResponseDTO1 = new EmployeeResponseDTO();
        employeeResponseDTO1.setId(2L);
        employeeResponseDTO1.setName("Jane Doe");
        employeeResponseDTO1.setEmail("janedoe@example.com");
        employeeResponseDTO1.setPassword("password65432");
        employeeResponseDTO1.setPasswordStrengthValue(80L);

        List<EmployeeResponseDTO> employeeList = Arrays.asList(employeeResponseDTO, employeeResponseDTO1);
        when(employeeService.getAllEmployees()).thenReturn(employeeList);

        mockMvc.perform(get("/api/employee/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(EMPLOYEE_NAME))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @WithMockUser
    void testGetByName() throws Exception {
        when(employeeService.getByName(EMPLOYEE_NAME)).thenReturn(Collections.singletonList(employeeResponseDTO));

        mockMvc.perform(get("/api/employee/name")
                        .param("name", EMPLOYEE_NAME))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(EMPLOYEE_NAME));

        verify(employeeService, times(1)).getByName(EMPLOYEE_NAME);
    }

    @Test
    @WithMockUser
    void testGetDependents() throws Exception {
        DependentDTO dependentDTO = new DependentDTO();
        dependentDTO.setId(1L);
        dependentDTO.setName("Jane Doe");

        List<DependentDTO> dependentsList = List.of(dependentDTO);
        when(employeeService.getDependents(EMPLOYEE_ID)).thenReturn(dependentsList);

        mockMvc.perform(get("/api/employee/dependents/{id}", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"));

        verify(employeeService, times(1)).getDependents(EMPLOYEE_ID);
    }

    @Test
    @WithMockUser
    void testGetScore() throws Exception {
        PasswordResponseDTO passwordResponseDTO = new PasswordResponseDTO();
        passwordResponseDTO.setValue(75L);
        passwordResponseDTO.setText("Boa");

        when(employeeService.getScore(any(PasswordRequestDTO.class))).thenReturn(passwordResponseDTO);

        mockMvc.perform(post("/api/employee/score")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"item\": \"password123\" }"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.label").value("Good"))
                .andExpect(jsonPath("$.score").value(75));

        verify(employeeService, times(1)).getScore(any(PasswordRequestDTO.class));
    }

    @Test
    @WithMockUser
    void testUpdateEmployee() throws Exception {
        doNothing().when(employeeService).updateEmployee(eq(EMPLOYEE_ID), any(EmployeeRequestDTO.class));

        // Perform PUT request
        mockMvc.perform(put("/api/employee/{id}", EMPLOYEE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"password\": \"password123\" }"))
                .andExpect(status().isCreated());

        // Verify the service call
        verify(employeeService, times(1)).updateEmployee(eq(EMPLOYEE_ID), any(EmployeeRequestDTO.class));
    }

    @Test
    @WithMockUser
    void testDeleteEmployee() throws Exception {
        // Mock the service call
        doNothing().when(employeeService).deleteEmployee(EMPLOYEE_ID);

        // Perform DELETE request
        mockMvc.perform(delete("/api/employee/{id}", EMPLOYEE_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify the service call
        verify(employeeService, times(1)).deleteEmployee(EMPLOYEE_ID);
    }

    // Helper methods for mock data
    public static EmployeeResponseDTO createMockEmployeeDTO() {
        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
        employeeResponseDTO.setId(1L);
        employeeResponseDTO.setName("John Doe");
        employeeResponseDTO.setEmail("john.doe@example.com");
        employeeResponseDTO.setPassword("password123");
        employeeResponseDTO.setPasswordStrengthValue(75L);
        return employeeResponseDTO;
    }

}

