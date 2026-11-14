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
