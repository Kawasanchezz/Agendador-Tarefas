package com.agendador.bff.client;

import com.agendador.bff.dto.Dtos.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioClient {

    private static final String HEADER = "X-Usuario-Email";

    private final RestClient http;

    public UsuarioClient(@Qualifier("usuarioRestClient") RestClient http) {
        this.http = http;
    }

    public UsuarioResponse criar(UsuarioRequest req) {
        return http.post().uri("/usuarios").body(req).retrieve().body(UsuarioResponse.class);
    }

    public void validarCredenciais(LoginRequest req) {
        http.post().uri("/usuarios/credenciais/validar").body(req).retrieve().toBodilessEntity();
    }

    public UsuarioResponse buscar(String email) {
        return http.get().uri(u -> u.path("/usuarios").queryParam("email", email).build())
                .retrieve().body(UsuarioResponse.class);
    }

    public UsuarioResponse atualizar(String email, UsuarioUpdateRequest req) {
        return http.put().uri("/usuarios").header(HEADER, email).body(req)
                .retrieve().body(UsuarioResponse.class);
    }

    public void deletar(String email) {
        http.delete().uri(u -> u.path("/usuarios/{email}").build(email)).retrieve().toBodilessEntity();
    }

    public EnderecoDto adicionarEndereco(String email, EnderecoDto dto) {
        return http.post().uri("/usuarios/endereco").header(HEADER, email).body(dto)
                .retrieve().body(EnderecoDto.class);
    }

    public EnderecoDto atualizarEndereco(String email, Long id, EnderecoDto dto) {
        return http.put().uri(u -> u.path("/usuarios/endereco").queryParam("id", id).build())
                .header(HEADER, email).body(dto).retrieve().body(EnderecoDto.class);
    }

    public TelefoneDto adicionarTelefone(String email, TelefoneDto dto) {
        return http.post().uri("/usuarios/telefone").header(HEADER, email).body(dto)
                .retrieve().body(TelefoneDto.class);
    }

    public TelefoneDto atualizarTelefone(String email, Long id, TelefoneDto dto) {
        return http.put().uri(u -> u.path("/usuarios/telefone").queryParam("id", id).build())
                .header(HEADER, email).body(dto).retrieve().body(TelefoneDto.class);
    }
}
