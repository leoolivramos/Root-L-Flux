package com.rootl.flux.infrastructure.adapter.in.rest;

import com.rootl.flux.domain.exception.DuplicateEventException;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.exception.FlowNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.rootl.flux.infrastructure.adapter.in.rest")
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EventValidationException.class)
    public ProblemDetail handleEventValidation(EventValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Erro de Validação de Evento");
        problem.setType(URI.create("urn:root-l-flux:erro:validacao"));
        return problem;
    }

    @ExceptionHandler(DuplicateEventException.class)
    public ProblemDetail handleDuplicateEvent(DuplicateEventException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Evento Duplicado Detectado");
        problem.setType(URI.create("urn:root-l-flux:erro:duplicado"));
        problem.setProperty("idempotencyKey", ex.getIdempotencyKey());
        return problem;
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Violação de unicidade ou concorrência detectada (chave de idempotência já processada)");
        problem.setTitle("Evento Duplicado Detectado");
        problem.setType(URI.create("urn:root-l-flux:erro:duplicado"));
        return problem;
    }

    @ExceptionHandler(FlowNotFoundException.class)
    public ProblemDetail handleFlowNotFound(FlowNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Fluxo Não Encontrado");
        problem.setType(URI.create("urn:root-l-flux:erro:nao-encontrado"));
        return problem;
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso Não Encontrado");
        problem.setType(URI.create("urn:root-l-flux:erro:nao-encontrado"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Falha na validação dos campos da requisição");
        problem.setTitle("Falha de Validação nos Parâmetros");
        problem.setType(URI.create("urn:root-l-flux:erro:validacao-parametros"));

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errosCampos", errors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Exceção não tratada capturada no GlobalExceptionHandler: ", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado no processamento da solicitação");
        problem.setTitle("Erro Interno do Servidor");
        problem.setType(URI.create("urn:root-l-flux:erro:interno"));
        return problem;
    }
}
