package com.example.user_service.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private ErrorDetails createErrorDetails(String key,
                                            WebRequest request,
                                            HttpStatus status,
                                            String objectName,
                                            String errorCode) {
        Locale locale = request.getLocale();
        String localizedMessage = messageSource.getMessage(
                key,
                null,
                status.getReasonPhrase(),
                locale
        );
        String requestUri = request.getDescription(false).replace("uri=", "");
        return new ErrorDetails(
                errorCode,
                localizedMessage,
                requestUri,
                status.value(),
                objectName
        );
    }

    private String buildLocalizedValidationMessage(List<? extends MessageSourceResolvable> errors,
                                                   Locale locale) {
        return errors.stream()
                .map(error -> messageSource.getMessage(error, locale))
                .collect(Collectors.joining(", "));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                              WebRequest request) {
        Locale locale = request.getLocale();
        String errorMessage = buildLocalizedValidationMessage(ex.getBindingResult().getAllErrors(), locale);

        log.warn("Validation failed | uri={} | message={}",
                request.getDescription(false).replace("uri=", ""), errorMessage);

        return new ErrorDetails(
                "VALIDATION-400",
                errorMessage,
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.value(),
                "Validation"
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                               WebRequest request) {
        Locale locale = request.getLocale();
        String errorMessage = buildLocalizedValidationMessage(ex.getAllErrors(), locale);

        log.warn("Validation failed | uri={} | message={}",
                request.getDescription(false).replace("uri=", ""), errorMessage);

        return new ErrorDetails(
                "VALIDATION-400",
                errorMessage,
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST.value(),
                "Validation"
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        String uri = request.getDescription(false).replace("uri=", "");
        log.warn("NOT_FOUND business error | uri={} | message={}", uri, ex.getMessage());

        return createErrorDetails("error.not_found.user", request, HttpStatus.NOT_FOUND, "UserEntity", "USER-404");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorDetails handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        String uri = request.getDescription(false).replace("uri=", "");
        log.warn("UNAUTHORIZED business error | uri={} | message={}", uri, ex.getMessage());

        return createErrorDetails(
                "error.invalid_credentials",
                request,
                HttpStatus.UNAUTHORIZED,
                "AuthService",
                "AUTH-401"
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDetails handleUnexpectedException(Exception e, WebRequest request) {
        String uri = request.getDescription(false).replace("uri=", "");
        log.error("INTERNAL_SERVER_ERROR unexpected exception | uri={}", uri, e);

        return new ErrorDetails(
                "GENERIC-500",
                "An unexpected error occurred",
                uri,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "InternalServerError"
        );
    }

}
