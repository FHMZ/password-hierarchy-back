package com.hierarchy.password_hierarchy_back.controllers.advice;

import com.hierarchy.password_hierarchy_back.exceptions.EmployeeNotFoundException;
import com.hierarchy.password_hierarchy_back.models.dtos.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ControllerAdviceHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmployeeNotFoundException(EmployeeNotFoundException ex, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

}
