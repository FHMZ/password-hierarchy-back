package com.hierarchy.password_hierarchy_back.controllers;

import com.hierarchy.password_hierarchy_back.models.dtos.*;
import com.hierarchy.password_hierarchy_back.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@RequestBody EmployeeRequestDTO employeeRequest) {
        employeeService.createEmployee(employeeRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        final EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployees() {
        final List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @GetMapping("/name")
    public ResponseEntity<List<EmployeeResponseDTO>> getByName(@RequestParam String name) {
        final List<EmployeeResponseDTO> employees = employeeService.getByName(name);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @GetMapping("/dependents/{id}")
    public ResponseEntity<List<DependentDTO>> getDependents(@PathVariable Long id) {
        final List<DependentDTO> employees = employeeService.getDependents(id);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @PostMapping("/score")
    public ResponseEntity<PasswordResponseDTO> getScore(@RequestBody PasswordRequestDTO passwordResponse) {
        return new ResponseEntity<>(employeeService.getScore(passwordResponse), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequestDTO employeeRequest) {
        employeeService.updateEmployee(id, employeeRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
