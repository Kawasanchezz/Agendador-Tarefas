package com.agendador.tarefas.exception;

public class LimiteTarefasException extends RuntimeException {
    public LimiteTarefasException(int limite) {
        super("Limite de " + limite + " tarefas por usuário atingido");
    }
}
