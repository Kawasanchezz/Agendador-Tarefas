package com.agendador.tarefas.repository;

import com.agendador.tarefas.domain.Tarefa;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends MongoRepository<Tarefa, String> {

    List<Tarefa> findByEmailUsuarioAndDataEventoBetweenOrderByDataEvento(
            String emailUsuario, LocalDateTime inicio, LocalDateTime fim);

    Optional<Tarefa> findByIdAndEmailUsuario(String id, String emailUsuario);
}
