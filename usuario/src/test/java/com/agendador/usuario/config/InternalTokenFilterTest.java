package com.agendador.usuario.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalTokenFilterTest {

    private static final String TOKEN = "token-interno-de-teste-com-32-chars-ok";
    private final InternalTokenFilter filtro = new InternalTokenFilter(TOKEN, new ObjectMapper().findAndRegisterModules());
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    void rejeitaChamadaSemToken() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();

        filtro.doFilter(new MockHttpServletRequest("GET", "/x"), res, chain);

        assertEquals(401, res.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void rejeitaTokenErrado() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
        req.addHeader("X-Internal-Token", "outro");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filtro.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void aceitaTokenCorreto() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
        req.addHeader("X-Internal-Token", TOKEN);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filtro.doFilter(req, res, chain);

        assertEquals(200, res.getStatus());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void recusaTokenConfiguradoCurto() {
        assertThrows(IllegalStateException.class, () -> new InternalTokenFilter("curto", new ObjectMapper()));
    }
}
