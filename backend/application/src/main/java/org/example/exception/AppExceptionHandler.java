package org.example.exception;

import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.identity.exception.DuplicateUserException;
import org.identity.exception.RegistrationException;
import org.identity.exception.InvalidTokenException;
import org.identity.exception.InvalidVerificationStrategyException;
import org.identity.exception.InvalidUserStateException;
import org.identity.exception.DuplicateWebhookException;
import org.processing.exception.DuplicateBatchException;
import org.streaming.exception.RaceNotFound;
import org.streaming.exception.ExternalHTTPError;

@RestControllerAdvice(annotations = RestController.class)
public class AppExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(AppExceptionHandler.class);

    private ProblemDetail buildProblemDetail(
            HttpStatus status,
            String title,
            String uri,
            String detail
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                status,
                detail != null ? detail : title
        );

        pd.setTitle(title);
        pd.setType(URI.create(uri));
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status,
            String title,
            String module,
            String error,
            String detail
    ) {
        return buildProblemDetail(
                status,
                title,
                "https://" + module + ".ukma.edu.ua/errors/" + error,
                detail
        );
    }

    @ExceptionHandler({
            NoResourceFoundException.class,
            EntityNotFoundException.class,
            NoHandlerFoundException.class
    })
    public ProblemDetail handleNotFound(Exception ex) {
        return buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Entity or resource not found",
                "https://api.ukma.edu.ua/errors/not-found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex
    ) {
        ProblemDetail pd = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "https://api.ukma.edu.ua/errors/validation-error",
                "Validation failed for one or more fields"
        );

        Map<String, String> errors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                fieldError ->
                                        fieldError.getDefaultMessage() != null
                                                ? fieldError.getDefaultMessage()
                                                : "Invalid value",
                                (first, second) -> first
                        ));

        pd.setProperty("errors", errors);

        return pd;
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            InvalidTokenException.class,
            InvalidVerificationStrategyException.class
    })
    public ProblemDetail handleBadRequest(Exception ex) {
        String module = ex instanceof InvalidTokenException
                || ex instanceof InvalidVerificationStrategyException
                ? "identity"
                : "api";

        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad request or business rule error",
                "https://" + module + ".ukma.edu.ua/errors/bad-request",
                ex.getMessage()
        );
    }

    @ExceptionHandler({
            DuplicateUserException.class,
            RegistrationException.class,
            DuplicateWebhookException.class
    })
    public ProblemDetail handleIdentityConflict(RuntimeException ex) {
        return buildProblemDetail(
                HttpStatus.CONFLICT,
                "Resource conflict",
                "identity",
                "conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateBatchException.class)
    public ProblemDetail handleDuplicateBatch(
            DuplicateBatchException ex
    ) {
        return buildProblemDetail(
                HttpStatus.CONFLICT,
                "Resource conflict",
                "processing",
                "conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidUserStateException.class)
    public ProblemDetail handleInvalidState(
            InvalidUserStateException ex
    ) {
        return buildProblemDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Invalid business state",
                "identity",
                "invalid-state",
                ex.getMessage()
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class
    })
    public ProblemDetail handleBadCredentials(Exception ex) {
        return buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Bad credentials",
                "identity",
                "bad-credentials",
                ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            AccessDeniedException ex
    ) {
        return buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Access denied",
                "identity",
                "access-denied",
                ex.getMessage()
        );
    }

    @ExceptionHandler(RaceNotFound.class)
    public ProblemDetail handleRaceNotFoundOnProvider(
            RaceNotFound ex
    ) {
        return buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "F1 race is not found for this info",
                "streaming",
                "bad-request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExternalHTTPError.class)
    public ProblemDetail handleExternalHTTPError(
            ExternalHTTPError ex
    ) {
        log.warn("External provider request failed", ex);

        return buildProblemDetail(
                HttpStatus.BAD_GATEWAY,
                "Provider API is not responding, try again later",
                "streaming",
                "bad-request",
                "The external provider is temporarily unavailable"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unexpected application error", ex);

        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "https://api.ukma.edu.ua/errors/internal-server-error",
                "An unexpected internal error occurred"
        );
    }
}
