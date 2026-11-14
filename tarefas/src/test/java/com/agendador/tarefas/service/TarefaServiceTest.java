package com.agendador.tarefas.service;

import com.agendador.tarefas.domain.StatusTarefa;
import com.agendador.tarefas.domain.Tarefa;
import com.agendador.tarefas.dto.Dtos.TarefaRequest;
import com.agendador.tarefas.dto.Dtos.TarefaResponse;
import com.agendador.tarefas.exception.TarefaNaoEncontradaException;
import com.agendador.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    TarefaRepository repository;
    @InjectMocks
    TarefaService service;

    @Test
    void criaTarefaPendenteDoUsuario() {
        when(repository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        TarefaResponse r = service.criar("a@a.com",
                new TarefaRequest("Reunião", "desc", LocalDateTime.now().plusDays(1)));

        assertEquals(StatusTarefa.PENDENTE, r.status());
        assertEquals("a@a.com", r.emailUsuario());
    }

    @Test
    void naoAlteraTarefaDeOutroUsuario() {
        when(repository.findByIdAndEmailUsuario("1", "b@b.com")).thenReturn(Optional.empty());

        assertThrows(TarefaNaoEncontradaException.class,
                () -> service.alterarStatus("b@b.com", "1", StatusTarefa.CONCLUIDA));
    }

    @Test
    void listagemTemTetoDeResultados() {
        service.listar("a@a.com", null, null);

        org.mockito.Mockito.verify(repository).findByEmailUsuarioAndDataEventoBetweenOrderByDataEvento(
                org.mockito.ArgumentMatchers.eq("a@a.com"), any(), any(),
                org.mockito.ArgumentMatchers.eq(org.springframework.data.domain.PageRequest.of(0, 500)));
    }

    @Test
    void deletarTodasRemoveSoDoUsuario() {
        service.deletarTodas("a@a.com");

        org.mockito.Mockito.verify(repository).deleteByEmailUsuario("a@a.com");
    }

    @Test
    void recusaCriarAlemDoLimiteDeTarefas() {
        when(repository.countByEmailUsuario("a@a.com")).thenReturn(1000L);

        assertThrows(com.agendador.tarefas.exception.LimiteTarefasException.class, () -> service.criar("a@a.com",
                new TarefaRequest("x", null, LocalDateTime.now().plusDays(1))));
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void alteraStatus() {
        Tarefa t = new Tarefa();
        t.setStatus(StatusTarefa.PENDENTE);
        when(repository.findByIdAndEmailUsuario("1", "a@a.com")).thenReturn(Optional.of(t));
        when(repository.save(t)).thenReturn(t);

        assertEquals(StatusTarefa.CONCLUIDA, service.alterarStatus("a@a.com", "1", StatusTarefa.CONCLUIDA).status());
    }
}
