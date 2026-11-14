package com.agendador.usuario.exception;

public final class Excecoes {

    private Excecoes() {
    }

    public static class ConflitoException extends RuntimeException {
        public ConflitoException(String msg) {
            super(msg);
        }
    }

    public static class NaoEncontradoException extends RuntimeException {
        public NaoEncontradoException(String msg) {
            super(msg);
        }
    }

    public static class NaoAutorizadoException extends RuntimeException {
        public NaoAutorizadoException(String msg) {
            super(msg);
        }
    }
}
