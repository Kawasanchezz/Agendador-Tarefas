package com.agendador.usuario.service;

import com.agendador.usuario.domain.Endereco;
import com.agendador.usuario.domain.Telefone;
import com.agendador.usuario.domain.Usuario;
import com.agendador.usuario.dto.Dtos.*;
import com.agendador.usuario.exception.Excecoes.ConflitoException;
import com.agendador.usuario.exception.Excecoes.NaoAutorizadoException;
import com.agendador.usuario.exception.Excecoes.NaoEncontradoException;
import com.agendador.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    /** Hash descartável: gasta o mesmo tempo de BCrypt quando o e-mail não existe (evita enumeração por tempo). */
    private final String hashFalso;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
        this.hashFalso = encoder.encode("hash-falso-para-igualar-tempo");
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        String email = normalizar(req.email());
        if (repository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome());
        u.setEmail(email);
        u.setSenha(encoder.encode(req.senha()));
        return UsuarioResponse.from(repository.save(u));
    }

    @Transactional(readOnly = true)
    public void validarCredenciais(LoginRequest req) {
        Usuario u = repository.findByEmail(normalizar(req.email())).orElse(null);
        if (u == null) {
            encoder.matches(req.senha(), hashFalso);
            throw new NaoAutorizadoException("Credenciais inválidas");
        }
        if (!encoder.matches(req.senha(), u.getSenha())) {
            throw new NaoAutorizadoException("Credenciais inválidas");
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(String email) {
        return UsuarioResponse.from(obter(email));
    }

    @Transactional
    public UsuarioResponse atualizar(String email, UsuarioUpdateRequest req) {
        Usuario u = obter(email);
        if (req.nome() != null && !req.nome().isBlank()) {
            u.setNome(req.nome());
        }
        if (req.senha() != null) {
            u.setSenha(encoder.encode(req.senha()));
        }
        return UsuarioResponse.from(u);
    }

    @Transactional
    public void deletar(String email) {
        repository.delete(obter(email));
    }

    @Transactional
    public EnderecoDto adicionarEndereco(String email, EnderecoDto dto) {
        Usuario u = obter(email);
        Endereco e = new Endereco();
        e.setUsuario(u);
        copiar(dto, e);
        u.getEnderecos().add(e);
        repository.flush();
        return EnderecoDto.from(e);
    }

    @Transactional
    public EnderecoDto atualizarEndereco(String email, Long id, EnderecoDto dto) {
        Endereco e = obter(email).getEnderecos().stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Endereço não encontrado"));
        copiar(dto, e);
        return EnderecoDto.from(e);
    }

    @Transactional
    public TelefoneDto adicionarTelefone(String email, TelefoneDto dto) {
        Usuario u = obter(email);
        Telefone t = new Telefone();
        t.setUsuario(u);
        t.setDdd(dto.ddd());
        t.setNumero(dto.numero());
        u.getTelefones().add(t);
        repository.flush();
        return TelefoneDto.from(t);
    }

    @Transactional
    public TelefoneDto atualizarTelefone(String email, Long id, TelefoneDto dto) {
        Telefone t = obter(email).getTelefones().stream()
                .filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Telefone não encontrado"));
        t.setDdd(dto.ddd());
        t.setNumero(dto.numero());
        return TelefoneDto.from(t);
    }

    private Usuario obter(String email) {
        return repository.findByEmail(normalizar(email))
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
    }

    private void copiar(EnderecoDto dto, Endereco e) {
        e.setRua(dto.rua());
        e.setNumero(dto.numero());
        e.setComplemento(dto.complemento());
        e.setCidade(dto.cidade());
        e.setEstado(dto.estado().toUpperCase(Locale.ROOT));
        e.setCep(dto.cep());
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
