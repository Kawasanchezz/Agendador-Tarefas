package com.agendador.tarefas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponse(Instant timestamp, int status, String mensagem, String path) {
    }

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    ResponseEntity<ErroResponse> naoEncontrada(TarefaNaoEncontradaException e, HttpServletRequest r) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage(), r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e, HttpServletRequest r) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, msg, r);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ErroResponse> requisicaoInvalida(Exception e, HttpServletRequest r) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida", r);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> generico(Exception e, HttpServletRequest r) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", r);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus s, String msg, HttpServletRequest r) {
        return ResponseEntity.status(s).body(new ErroResponse(Instant.now(), s.value(), msg, r.getRequestURI()));
    }
}
