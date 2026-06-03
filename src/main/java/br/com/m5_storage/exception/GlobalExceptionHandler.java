package br.com.m5_storage.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────

    @ExceptionHandler(IdNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            IdNaoEncontradoException ex,
            HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                ex.getMessage(), request);
    }

    // ── 403 Forbidden — permissão insuficiente ─────────────────

    @ExceptionHandler(OperadorNecessarioException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            OperadorNecessarioException ex,
            HttpServletRequest request) {

        return build(HttpStatus.FORBIDDEN, "Acesso negado",
                ex.getMessage(), request);
    }

    // ── 400 Bad Request — validação de campos ──────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    errors.put(field, message);
                });

        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                request.getRequestURI(),
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ── 400 Bad Request — regras de negócio ───────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Operação inválida",
                ex.getMessage(), request);
    }

    // ── 400 Bad Request — constraint violation ─────────────────

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Violação de restrição",
                ex.getMessage(), request);
    }

    // ── 409 Conflict — integridade do banco ───────────────────

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return build(HttpStatus.CONFLICT,
                "Erro de integridade no banco de dados",
                "Já existe um registro com esses dados ou existe relacionamento inválido.",
                request);
    }

    // ── 415 Unsupported Media Type ────────────────────────────

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de conteúdo não suportado",
                "Utilize Content-Type: application/json",
                request);
    }

    // ── 405 Method Not Allowed ────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não permitido",
                ex.getMessage(), request);
    }

    // ── 400 Missing Path Variable ─────────────────────────────

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPath(
            MissingPathVariableException ex,
            HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST,
                "Parâmetro da URL ausente",
                ex.getMessage(), request);
    }

    // ── 500 Internal Server Error ─────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor",
                ex.getMessage(), request);
    }

    // ── builder helper ────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                String error,
                                                String message,
                                                HttpServletRequest request) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        status.value(), error, message,
                        request.getRequestURI(), LocalDateTime.now()
                ));
    }

    // ── response bodies ───────────────────────────────────────

    public record ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            LocalDateTime timestamp
    ) {}

    public record ValidationErrorResponse(
            int status,
            String error,
            String path,
            LocalDateTime timestamp,
            Map<String, String> fields
    ) {}
}