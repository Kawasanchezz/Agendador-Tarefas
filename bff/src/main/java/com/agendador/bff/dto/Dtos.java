package com.agendador.bff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {

    private Dtos() {
    }

    // ---------- autenticação / usuário ----------

    public record UsuarioRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 6, max = 72) String senha) {
    }

    public record UsuarioUpdateRequest(String nome, @Size(min = 6, max = 72) String senha) {
    }

    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank @Size(max = 72) String senha) {
    }

    public record TokenResponse(String token, String tipo, long expiraEmMs) {
    }

    public record EnderecoDto(
            Long id,
            @NotBlank String rua,
            @NotNull Long numero,
            String complemento,
            @NotBlank String cidade,
            @NotBlank @Size(min = 2, max = 2) String estado,
            @NotBlank @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String cep) {
    }

    public record TelefoneDto(
            Long id,
            @NotBlank @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos") String ddd,
            @NotBlank @Pattern(regexp = "\\d{8,9}", message = "Número deve ter 8 ou 9 dígitos") String numero) {
    }

    public record UsuarioResponse(Long id, String nome, String email,
                                  List<EnderecoDto> enderecos, List<TelefoneDto> telefones) {
    }

    // ---------- tarefas ----------

    public enum StatusTarefa {PENDENTE, EM_ANDAMENTO, CONCLUIDA, CANCELADA}

    public record TarefaRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 1000) String descricao,
            @NotNull @Future @Schema(example = "2030-01-15T10:00:00") LocalDateTime dataEvento) {
    }

    public record TarefaResponse(String id, String nome, String descricao, LocalDateTime dataEvento,
                                 LocalDateTime dataCriacao, StatusTarefa status, String emailUsuario) {
    }

    // ---------- CEP ----------

    public record EnderecoCepResponse(String cep, String logradouro, String complemento, String bairro,
                                      String cidade, String estado) {
    }
}
