package com.agendador.tarefas.dto;

import com.agendador.tarefas.domain.StatusTarefa;
import com.agendador.tarefas.domain.Tarefa;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class Dtos {

    private Dtos() {
    }

    public record TarefaRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 1000) String descricao,
            @NotNull @Future LocalDateTime dataEvento) {
    }

    public record TarefaResponse(String id, String nome, String descricao, LocalDateTime dataEvento,
                                 LocalDateTime dataCriacao, StatusTarefa status, String emailUsuario) {

        public static TarefaResponse from(Tarefa t) {
            return new TarefaResponse(t.getId(), t.getNome(), t.getDescricao(), t.getDataEvento(),
                    t.getDataCriacao(), t.getStatus(), t.getEmailUsuario());
        }
    }
}
