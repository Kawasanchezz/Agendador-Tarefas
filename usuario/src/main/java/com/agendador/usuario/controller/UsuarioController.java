package com.agendador.usuario.controller;

import com.agendador.usuario.dto.Dtos.*;
import com.agendador.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * API interna: só o BFF a chama. O e-mail do usuário autenticado chega no header X-Usuario-Email.
 */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final String HEADER = "X-Usuario-Email";

    private final UsuarioService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest req) {
        return service.criar(req);
    }

    @PostMapping("/credenciais/validar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validar(@Valid @RequestBody LoginRequest req) {
        service.validarCredenciais(req);
    }

    @GetMapping
    public UsuarioResponse buscar(@RequestParam String email) {
        return service.buscar(email);
    }

    @PutMapping
    public UsuarioResponse atualizar(@RequestHeader(HEADER) String email,
                                     @Valid @RequestBody UsuarioUpdateRequest req) {
        return service.atualizar(email, req);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable String email) {
        service.deletar(email);
    }

    @PostMapping("/endereco")
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoDto adicionarEndereco(@RequestHeader(HEADER) String email,
                                         @Valid @RequestBody EnderecoDto dto) {
        return service.adicionarEndereco(email, dto);
    }

    @PutMapping("/endereco")
    public EnderecoDto atualizarEndereco(@RequestHeader(HEADER) String email, @RequestParam Long id,
                                         @Valid @RequestBody EnderecoDto dto) {
        return service.atualizarEndereco(email, id, dto);
    }

    @PostMapping("/telefone")
    @ResponseStatus(HttpStatus.CREATED)
    public TelefoneDto adicionarTelefone(@RequestHeader(HEADER) String email,
                                         @Valid @RequestBody TelefoneDto dto) {
        return service.adicionarTelefone(email, dto);
    }

    @PutMapping("/telefone")
    public TelefoneDto atualizarTelefone(@RequestHeader(HEADER) String email, @RequestParam Long id,
                                         @Valid @RequestBody TelefoneDto dto) {
        return service.atualizarTelefone(email, id, dto);
    }
}
