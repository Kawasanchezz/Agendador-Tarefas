package com.agendador.usuario.exception;

import com.agendador.usuario.exception.Excecoes.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponse(Instant timestamp, int status, String mensagem, String path) {
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResponse> conflito(ConflitoException e, HttpServletRequest r) {
        return erro(HttpStatus.CONFLICT, e.getMessage(), r);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException e, HttpServletRequest r) {
        return erro(HttpStatus.CONFLICT, "Violação de integridade dos dados", r);
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException e, HttpServletRequest r) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage(), r);
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    ResponseEntity<ErroResponse> naoAutorizado(NaoAutorizadoException e, HttpServletRequest r) {
        return erro(HttpStatus.UNAUTHORIZED, e.getMessage(), r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e, HttpServletRequest r) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, msg, r);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class})
    ResponseEntity<ErroResponse> requisicaoInvalida(Exception e, HttpServletRequest r) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida", r);
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class})
    ResponseEntity<ErroResponse> rotaOuMetodo(Exception e, HttpServletRequest r) {
        HttpStatus s = HttpStatus.valueOf(((ErrorResponse) e).getStatusCode().value());
        return erro(s, s.getReasonPhrase(), r);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> generico(Exception e, HttpServletRequest r) {
        log.error("Erro não tratado em {}", r.getRequestURI(), e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", r);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus s, String msg, HttpServletRequest r) {
        return ResponseEntity.status(s).body(new ErroResponse(Instant.now(), s.value(), msg, r.getRequestURI()));
    }
}
