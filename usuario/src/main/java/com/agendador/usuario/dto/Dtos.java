package com.agendador.usuario.dto;

import com.agendador.usuario.domain.Endereco;
import com.agendador.usuario.domain.Telefone;
import com.agendador.usuario.domain.Usuario;
import jakarta.validation.constraints.*;

import java.util.List;

public final class Dtos {

    private Dtos() {
    }

    public record UsuarioRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 6, max = 72) String senha) {
    }

    public record UsuarioUpdateRequest(
            @Size(max = 120) String nome,
            @Size(min = 6, max = 72) String senha,
            @Size(max = 72) String senhaAtual) {
    }

    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank @Size(max = 72) String senha) {
    }

    public record EnderecoDto(
            Long id,
            @NotBlank String rua,
            @NotNull Long numero,
            String complemento,
            @NotBlank String cidade,
            @NotBlank @Size(min = 2, max = 2) String estado,
            @NotBlank @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String cep) {

        public static EnderecoDto from(Endereco e) {
            return new EnderecoDto(e.getId(), e.getRua(), e.getNumero(), e.getComplemento(),
                    e.getCidade(), e.getEstado(), e.getCep());
        }
    }

    public record TelefoneDto(
            Long id,
            @NotBlank @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos") String ddd,
            @NotBlank @Pattern(regexp = "\\d{8,9}", message = "Número deve ter 8 ou 9 dígitos") String numero) {

        public static TelefoneDto from(Telefone t) {
            return new TelefoneDto(t.getId(), t.getDdd(), t.getNumero());
        }
    }

    public record UsuarioResponse(Long id, String nome, String email,
                                  List<EnderecoDto> enderecos, List<TelefoneDto> telefones) {

        public static UsuarioResponse from(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(),
                    u.getEnderecos().stream().map(EnderecoDto::from).toList(),
                    u.getTelefones().stream().map(TelefoneDto::from).toList());
        }
    }
}
