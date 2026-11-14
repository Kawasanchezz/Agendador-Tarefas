package com.agendador.bff.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Não é um @Component de propósito: é instanciado em SecurityConfig para rodar só dentro
 * da cadeia do Spring Security (evita registro duplicado como filtro de servlet).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RevogacaoService revogacao;

    public JwtAuthFilter(JwtService jwtService, RevogacaoService revogacao) {
        this.jwtService = jwtService;
        this.revogacao = revogacao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.validar(header.substring(7))
                    .filter(t -> !revogacao.estaRevogado(t.email(), t.emitidoEm()))
                    .ifPresent(t -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(t.email(), null, List.of())));
        }
        chain.doFilter(request, response);
    }
}
