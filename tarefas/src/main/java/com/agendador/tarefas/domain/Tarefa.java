package com.agendador.tarefas.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("tarefas")
@Getter
@Setter
@NoArgsConstructor
public class Tarefa {

    @Id
    private String id;
    private String nome;
    private String descricao;
    private LocalDateTime dataEvento;
    private LocalDateTime dataCriacao;
    private StatusTarefa status;

    @Indexed
    private String emailUsuario;
}
