package com.agendador.tarefas.service;

import com.agendador.tarefas.domain.StatusTarefa;
import com.agendador.tarefas.domain.Tarefa;
import com.agendador.tarefas.dto.Dtos.TarefaRequest;
import com.agendador.tarefas.dto.Dtos.TarefaResponse;
import com.agendador.tarefas.exception.LimiteTarefasException;
import com.agendador.tarefas.exception.TarefaNaoEncontradaException;
import com.agendador.tarefas.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    /** Teto de resultados por listagem, para evitar respostas ilimitadas. */
    static final int LIMITE_LISTAGEM = 500;
    static final int MAX_TAREFAS_POR_USUARIO = 1000;

    private final TarefaRepository repository;

    public TarefaResponse criar(String email, TarefaRequest req) {
        if (repository.countByEmailUsuario(email) >= MAX_TAREFAS_POR_USUARIO) {
            throw new LimiteTarefasException(MAX_TAREFAS_POR_USUARIO);
        }
        Tarefa t = new Tarefa();
        t.setNome(req.nome());
        t.setDescricao(req.descricao());
        t.setDataEvento(req.dataEvento());
        t.setDataCriacao(LocalDateTime.now());
        t.setStatus(StatusTarefa.PENDENTE);
        t.setEmailUsuario(email);
        return TarefaResponse.from(repository.save(t));
    }

    public List<TarefaResponse> listar(String email, LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime de = inicio != null ? inicio : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime ate = fim != null ? fim : LocalDateTime.of(9999, 12, 31, 23, 59);
        return repository.findByEmailUsuarioAndDataEventoBetweenOrderByDataEvento(
                        email, de, ate, PageRequest.of(0, LIMITE_LISTAGEM))
                .stream().map(TarefaResponse::from).toList();
    }

    public TarefaResponse detalhar(String email, String id) {
        return TarefaResponse.from(obter(email, id));
    }

    public TarefaResponse alterarStatus(String email, String id, StatusTarefa status) {
        Tarefa t = obter(email, id);
        t.setStatus(status);
        return TarefaResponse.from(repository.save(t));
    }

    public void deletarTodas(String email) {
        repository.deleteByEmailUsuario(email);
    }

    public void deletar(String email, String id) {
        repository.delete(obter(email, id));
    }

    private Tarefa obter(String email, String id) {
        return repository.findByIdAndEmailUsuario(id, email)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }
}
