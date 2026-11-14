package com.agendador.tarefas.exception;

public class TarefaNaoEncontradaException extends RuntimeException {
    public TarefaNaoEncontradaException(String id) {
        super("Tarefa não encontrada: " + id);
    }
}
