package com.agendador.bff.controller;

import com.agendador.bff.client.TarefaClient;
import com.agendador.bff.client.UsuarioClient;
import com.agendador.bff.dto.Dtos.*;
import com.agendador.bff.exception.Excecoes.AcessoNegadoException;
import com.agendador.bff.security.JwtService;
import com.agendador.bff.security.LoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Locale;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro, login e gerenciamento de perfil, endereços e telefones")
public class UsuarioController {

    private final UsuarioClient usuarios;
    private final TarefaClient tarefas;
    private final JwtService jwt;
    private final LoginAttemptService tentativas;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Cadastrar usuário (público)")
    public UsuarioResponse cadastrar(@Valid @RequestBody UsuarioRequest req) {
        return usuarios.criar(req);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autenticar e receber o JWT (público)")
    public TokenResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        String ip = http.getRemoteAddr();
        tentativas.verificar(email, ip);
        try {
            usuarios.validarCredenciais(req);
        } catch (HttpClientErrorException.Unauthorized e) {
            tentativas.registrarFalha(email, ip);
            throw e;
        }
        tentativas.registrarSucesso(email);
        String token = jwt.gerar(email);
        return new TokenResponse(token, "Bearer", jwt.getExpirationMs());
    }

    @GetMapping
    @Operation(summary = "Buscar o perfil (sem o parâmetro email, devolve o do usuário logado)")
    public UsuarioResponse buscar(@AuthenticationPrincipal String logado,
                                  @RequestParam(required = false) String email) {
        return usuarios.buscar(proprio(logado, email));
    }

    @PutMapping
    @Operation(summary = "Atualizar nome e/ou senha do usuário logado")
    public UsuarioResponse atualizar(@AuthenticationPrincipal String logado,
                                     @Valid @RequestBody UsuarioUpdateRequest req) {
        return usuarios.atualizar(logado, req);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir a própria conta e todas as suas tarefas")
    public void deletar(@AuthenticationPrincipal String logado, @PathVariable String email) {
        String dono = proprio(logado, email);
        usuarios.deletar(dono);
        tarefas.deletarTodas(dono);
    }

    @PostMapping("/endereco")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar endereço")
    public EnderecoDto adicionarEndereco(@AuthenticationPrincipal String logado,
                                         @Valid @RequestBody EnderecoDto dto) {
        return usuarios.adicionarEndereco(logado, dto);
    }

    @PutMapping("/endereco")
    @Operation(summary = "Atualizar endereço pelo id")
    public EnderecoDto atualizarEndereco(@AuthenticationPrincipal String logado, @RequestParam Long id,
                                         @Valid @RequestBody EnderecoDto dto) {
        return usuarios.atualizarEndereco(logado, id, dto);
    }

    @PostMapping("/telefone")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar telefone")
    public TelefoneDto adicionarTelefone(@AuthenticationPrincipal String logado,
                                         @Valid @RequestBody TelefoneDto dto) {
        return usuarios.adicionarTelefone(logado, dto);
    }

    @PutMapping("/telefone")
    @Operation(summary = "Atualizar telefone pelo id")
    public TelefoneDto atualizarTelefone(@AuthenticationPrincipal String logado, @RequestParam Long id,
                                         @Valid @RequestBody TelefoneDto dto) {
        return usuarios.atualizarTelefone(logado, id, dto);
    }

    /** Garante que o usuário só acesse o próprio cadastro. */
    private String proprio(String logado, String email) {
        if (email == null || email.isBlank()) {
            return logado;
        }
        if (!email.trim().equalsIgnoreCase(logado)) {
            throw new AcessoNegadoException("Você só pode acessar os próprios dados");
        }
        return logado;
    }
}
