package org.example.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateWebhookException.class)
    public ProblemDetail handleDuplicateWebhook(DuplicateWebhookException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Resource Duplicate Conflict");
        problem.setType(URI.create("https://streaming-gateway.ukma.edu/errors/duplicate"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "The requested endpoint does not exist"
        );

        pd.setTitle("Endpoint not found");
        pd.setType(URI.create(
                "https://streaming-gateway.ukma.edu.ua/errors/endpoint-not-found"
        ));
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFound(NoHandlerFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "The requested endpoint does not exist"
        );

        pd.setTitle("Endpoint not found");
        pd.setType(URI.create(
                "https://streaming-gateway.ukma.edu.ua/errors/endpoint-not-found"
        ));
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Entity not found");
        pd.setType(URI.create("https://streaming-gateway.ukma.edu.ua/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        pd.setTitle("Incorrect request");
        pd.setType(URI.create("https://streaming-gateway.ukma.edu.ua/errors/validation-error"));
        pd.setProperty("timestamp", Instant.now());

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage()
                                != null ? fe.getDefaultMessage() : "Not valid value",
                        (first, second) -> first));

        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Business rules error");
        pd.setType(URI.create("https://streaming-gateway.ukma.edu.ua/errors/illegal-argument"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Accepted message not readable");
        pd.setType(URI.create("https://streaming-gateway.ukma.edu.ua/errors/message-not-readable"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred");
        pd.setTitle("Internal server error");
        pd.setType(URI.create("https://streaming-gateway.ukma.edu.ua/errors/internal-server-error"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
