package com.agendador.bff;

import com.agendador.bff.client.TarefaClient;
import com.agendador.bff.client.UsuarioClient;
import com.agendador.bff.client.ViaCepClient;
import com.agendador.bff.exception.Excecoes.CepNaoEncontradoException;
import com.agendador.bff.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=chave-de-teste-com-mais-de-32-caracteres-ok",
        "jwt.expiration-ms=60000"})
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    JwtService jwtService;

    @MockitoBean
    UsuarioClient usuarioClient;
    @MockitoBean
    TarefaClient tarefaClient;
    @MockitoBean
    ViaCepClient viaCepClient;

    @Test
    void loginValidoRetornaTokenUsavelNasRotasProtegidas() throws Exception {
        String body = """
                {"email":"Ana@Mail.com","senha":"segredo1"}""";

        String resp = mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(resp);
        String token = json.get("token").asText();

        when(tarefaClient.listar("ana@mail.com", null, null)).thenReturn(java.util.List.of());
        mvc.perform(get("/tarefas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginComCredenciaisInvalidasRepassa401() throws Exception {
        doThrow(org.springframework.web.client.HttpClientErrorException.create(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "x", new org.springframework.http.HttpHeaders(),
                "{\"mensagem\":\"Credenciais inválidas\"}".getBytes(), null))
                .when(usuarioClient).validarCredenciais(org.mockito.ArgumentMatchers.any());

        mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@a.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));
    }

    @Test
    void bloqueiaLoginAposVariasFalhasDoMesmoEmail() throws Exception {
        doThrow(org.springframework.web.client.HttpClientErrorException.create(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "x", new org.springframework.http.HttpHeaders(),
                "{}".getBytes(), null))
                .when(usuarioClient).validarCredenciais(org.mockito.ArgumentMatchers.any());
        String body = "{\"email\":\"forca@bruta.com\",\"senha\":\"errada\"}";

        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests());
        org.mockito.Mockito.verify(usuarioClient, org.mockito.Mockito.times(5))
                .validarCredenciais(org.mockito.ArgumentMatchers.argThat(r -> "forca@bruta.com".equals(r.email())));
    }

    @Test
    void recusaSenhaGigante() throws Exception {
        String senha = "a".repeat(100_000);
        mvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@a.com\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tokenComSubjectVazioRetorna401() throws Exception {
        String token = jwtService.gerar(" ");
        mvc.perform(get("/tarefas").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void excluirContaApagaTambemAsTarefas() throws Exception {
        String token = jwtService.gerar("saindo@mail.com");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/usuarios/saindo@mail.com").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        org.mockito.Mockito.verify(usuarioClient).deletar("saindo@mail.com");
        org.mockito.Mockito.verify(tarefaClient).deletarTodas("saindo@mail.com");
    }

    @Test
    void naoExcluiContaDeOutroUsuario() throws Exception {
        String token = jwtService.gerar("ana@mail.com");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/usuarios/vitima@mail.com").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        org.mockito.Mockito.verifyNoInteractions(tarefaClient);
    }

    @Test
    void rotaInexistenteRetorna404EMetodoErradoRetorna405() throws Exception {
        String token = jwtService.gerar("ana@mail.com");
        mvc.perform(get("/nao-existe").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/tarefas/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401() throws Exception {
        mvc.perform(get("/tarefas")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/tarefas").header("Authorization", "Bearer lixo")).andExpect(status().isUnauthorized());
    }

    @Test
    void naoPodeLerPerfilDeOutroUsuario() throws Exception {
        String token = jwtService.gerar("ana@mail.com");
        mvc.perform(get("/usuarios").param("email", "outro@mail.com").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void validaCadastro() throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"x\",\"senha\":\"1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cepInvalidoRetorna400ECepInexistenteRetorna404() throws Exception {
        String token = jwtService.gerar("ana@mail.com");
        mvc.perform(get("/endereco/123").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        when(viaCepClient.buscar("99999999")).thenThrow(new CepNaoEncontradoException("99999999"));
        mvc.perform(get("/endereco/99999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
