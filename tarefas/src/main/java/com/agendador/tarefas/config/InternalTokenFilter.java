package com.agendador.tarefas.config;

import com.agendador.tarefas.exception.GlobalExceptionHandler.ErroResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

/** Aceita apenas chamadas que carregam o token compartilhado com o BFF (header X-Internal-Token). */
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Internal-Token";

    private final byte[] esperado;
    private final ObjectMapper mapper;

    public InternalTokenFilter(@Value("${internal.token}") String token, ObjectMapper mapper) {
        if (token == null || token.length() < 32) {
            throw new IllegalStateException("internal.token deve ter pelo menos 32 caracteres");
        }
        this.esperado = token.getBytes(StandardCharsets.UTF_8);
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String recebido = req.getHeader(HEADER);
        if (recebido == null || !MessageDigest.isEqual(recebido.getBytes(StandardCharsets.UTF_8), esperado)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            mapper.writeValue(res.getWriter(), new ErroResponse(Instant.now(), 401,
                    "Acesso interno não autorizado", req.getRequestURI()));
            return;
        }
        chain.doFilter(req, res);
    }
}
