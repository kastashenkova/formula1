package org.example.exception;

import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String uri, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create(uri));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler({DuplicateUserException.class, RegistrationException.class})
    public ProblemDetail handleConflicts(RuntimeException ex) {
        return buildProblemDetail(
                HttpStatus.CONFLICT,
                "Resource conflict",
                "https://core.ukma.edu.ua/errors/conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler({InvalidTokenException.class,
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class})
    public ProblemDetail handleBadRequest(Exception ex) {
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad request or business rule error",
                "https://core.ukma.edu.ua/errors/bad-request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidUserStateException.class)
    public ProblemDetail handleInvalidState(InvalidUserStateException ex) {
        return buildProblemDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Invalid business state",
                "https://core.ukma.edu.ua/errors/invalid-state",
                ex.getMessage()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        return buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Entity not found",
                "https://core.ukma.edu.ua/errors/not-found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "https://core.ukma.edu.ua/errors/validation-error",
                "Validation failed for one or more fields"
        );

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage()
                                != null ? fe.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));

        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "https://core.ukma.edu.ua/errors/internal-server-error",
                "An unexpected internal error occurred"
        );
    }
}
