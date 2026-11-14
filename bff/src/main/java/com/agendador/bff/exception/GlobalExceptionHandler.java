package com.agendador.bff.exception;

import com.agendador.bff.exception.Excecoes.AcessoNegadoException;
import com.agendador.bff.exception.Excecoes.CepNaoEncontradoException;
import com.agendador.bff.exception.Excecoes.ServicoExternoException;
import com.agendador.bff.exception.Excecoes.TentativasExcedidasException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper mapper;

    public record ErroResponse(Instant timestamp, int status, String mensagem, String path) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e, HttpServletRequest r) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, msg, r);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErroResponse> validacaoParametro(HandlerMethodValidationException e, HttpServletRequest r) {
        String msg = e.getAllErrors().stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, msg, r);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErroResponse> violacao(ConstraintViolationException e, HttpServletRequest r) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage(), r);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ErroResponse> requisicaoInvalida(Exception e, HttpServletRequest r) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida", r);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException e, HttpServletRequest r) {
        return erro(HttpStatus.FORBIDDEN, e.getMessage(), r);
    }

    @ExceptionHandler(TentativasExcedidasException.class)
    ResponseEntity<ErroResponse> tentativas(TentativasExcedidasException e, HttpServletRequest r) {
        return erro(HttpStatus.TOO_MANY_REQUESTS, e.getMessage(), r);
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    ResponseEntity<ErroResponse> cep(CepNaoEncontradoException e, HttpServletRequest r) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage(), r);
    }

    @ExceptionHandler(ServicoExternoException.class)
    ResponseEntity<ErroResponse> externo(ServicoExternoException e, HttpServletRequest r) {
        return erro(HttpStatus.BAD_GATEWAY, e.getMessage(), r);
    }

    /** Erros devolvidos pelos serviços internos: 4xx é repassado, 5xx vira 502. */
    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ErroResponse> downstream(RestClientResponseException e, HttpServletRequest r) {
        HttpStatusCode status = e.getStatusCode();
        if (status.is4xxClientError()) {
            return erro(HttpStatus.valueOf(status.value()), mensagemDownstream(e), r);
        }
        return erro(HttpStatus.BAD_GATEWAY, "Serviço interno retornou erro", r);
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<ErroResponse> indisponivel(ResourceAccessException e, HttpServletRequest r) {
        return erro(HttpStatus.SERVICE_UNAVAILABLE, "Serviço interno indisponível", r);
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

    private String mensagemDownstream(RestClientResponseException e) {
        try {
            JsonNode n = mapper.readTree(e.getResponseBodyAsString());
            if (n.hasNonNull("mensagem")) {
                return n.get("mensagem").asText();
            }
        } catch (Exception ignored) {
            // corpo não é JSON: usa mensagem genérica
        }
        return "Requisição rejeitada pelo serviço interno";
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus s, String msg, HttpServletRequest r) {
        return ResponseEntity.status(s).body(new ErroResponse(Instant.now(), s.value(), msg, r.getRequestURI()));
    }
}
