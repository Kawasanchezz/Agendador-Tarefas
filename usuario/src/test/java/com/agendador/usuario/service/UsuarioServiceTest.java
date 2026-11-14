package com.agendador.usuario.service;

import com.agendador.usuario.domain.Usuario;
import com.agendador.usuario.dto.Dtos.LoginRequest;
import com.agendador.usuario.dto.Dtos.UsuarioRequest;
import com.agendador.usuario.dto.Dtos.UsuarioResponse;
import com.agendador.usuario.exception.Excecoes.ConflitoException;
import com.agendador.usuario.exception.Excecoes.NaoAutorizadoException;
import com.agendador.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repository;
    @Spy
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    @InjectMocks
    UsuarioService service;

    @Test
    void criaUsuarioComSenhaCriptografadaEEmailNormalizado() {
        when(repository.existsByEmail("a@a.com")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse r = service.criar(new UsuarioRequest("Ana", " A@A.com ", "segredo1"));

        assertEquals("a@a.com", r.email());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(u -> !u.getSenha().equals("segredo1")));
    }

    @Test
    void rejeitaEmailDuplicado() {
        when(repository.existsByEmail("a@a.com")).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.criar(new UsuarioRequest("Ana", "a@a.com", "segredo1")));
        verify(repository, never()).save(any());
    }

    @Test
    void emailInexistenteTambemExecutaBcryptParaIgualarTempo() {
        when(repository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        org.mockito.Mockito.clearInvocations(encoder);

        assertThrows(NaoAutorizadoException.class,
                () -> service.validarCredenciais(new LoginRequest("x@x.com", "qualquer1")));
        verify(encoder).matches(org.mockito.ArgumentMatchers.eq("qualquer1"), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void trocarSenhaExigeSenhaAtualCorreta() {
        Usuario u = new Usuario();
        u.setEmail("a@a.com");
        u.setSenha(encoder.encode("atual123"));
        when(repository.findByEmail("a@a.com")).thenReturn(Optional.of(u));

        assertThrows(com.agendador.usuario.exception.Excecoes.SenhaAtualIncorretaException.class,
                () -> service.atualizar("a@a.com", new com.agendador.usuario.dto.Dtos.UsuarioUpdateRequest(null, "nova1234", null)));
        assertThrows(com.agendador.usuario.exception.Excecoes.SenhaAtualIncorretaException.class,
                () -> service.atualizar("a@a.com", new com.agendador.usuario.dto.Dtos.UsuarioUpdateRequest(null, "nova1234", "errada")));
        assertTrue(encoder.matches("atual123", u.getSenha()));

        service.atualizar("a@a.com", new com.agendador.usuario.dto.Dtos.UsuarioUpdateRequest(null, "nova1234", "atual123"));
        assertTrue(encoder.matches("nova1234", u.getSenha()));
    }

    @Test
    void limitaEnderecosETelefonesPorUsuario() {
        Usuario u = new Usuario();
        u.setEmail("a@a.com");
        for (int i = 0; i < UsuarioService.MAX_ITENS; i++) {
            u.getEnderecos().add(new com.agendador.usuario.domain.Endereco());
            u.getTelefones().add(new com.agendador.usuario.domain.Telefone());
        }
        when(repository.findByEmail("a@a.com")).thenReturn(Optional.of(u));

        assertThrows(ConflitoException.class, () -> service.adicionarEndereco("a@a.com",
                new com.agendador.usuario.dto.Dtos.EnderecoDto(null, "Rua", 1L, null, "SP", "SP", "01001000")));
        assertThrows(ConflitoException.class, () -> service.adicionarTelefone("a@a.com",
                new com.agendador.usuario.dto.Dtos.TelefoneDto(null, "11", "987654321")));
    }

    @Test
    void rejeitaSenhaIncorreta() {
        Usuario u = new Usuario();
        u.setEmail("a@a.com");
        u.setSenha(encoder.encode("certa123"));
        when(repository.findByEmail("a@a.com")).thenReturn(Optional.of(u));

        assertThrows(NaoAutorizadoException.class,
                () -> service.validarCredenciais(new LoginRequest("a@a.com", "errada")));
        assertDoesNotThrow(() -> service.validarCredenciais(new LoginRequest("a@a.com", "certa123")));
    }
}
