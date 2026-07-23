package com.vzap.trytons.mapper;

import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;
@Provider
public class JAXExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(Collectors.joining(", ")); //CSV Format for build

    if (message.isBlank()) {
        message = "Invalid request structure";
    }

    ErrorResponseDTO error = ErrorResponseDTO.of(message, "VALIDATION_ERROR");

    return Response.status(Response.Status.BAD_REQUEST)
            .entity(error)
            .build();
    }
}
