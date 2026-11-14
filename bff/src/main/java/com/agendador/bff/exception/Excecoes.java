package com.agendador.bff.exception;

public final class Excecoes {

    private Excecoes() {
    }

    /** Usuário autenticado tentando acessar dados de outro usuário (403). */
    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String msg) {
            super(msg);
        }
    }

    /** CEP inexistente (404). */
    public static class CepNaoEncontradoException extends RuntimeException {
        public CepNaoEncontradoException(String cep) {
            super("CEP não encontrado: " + cep);
        }
    }

    /** Falha em serviço externo, como o ViaCEP (502). */
    public static class ServicoExternoException extends RuntimeException {
        public ServicoExternoException(String msg, Throwable causa) {
            super(msg, causa);
        }
    }
}
