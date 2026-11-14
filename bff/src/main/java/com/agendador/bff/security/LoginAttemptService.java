package com.agendador.bff.security;

import com.agendador.bff.exception.Excecoes.TentativasExcedidasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas de login falhas por e-mail e por IP (janela fixa, em memória).
 * Com várias instâncias do BFF o contador não é compartilhado: use Redis ou um gateway nesse caso.
 */
@Component
public class LoginAttemptService {

    static final int MAX_POR_EMAIL = 5;
    static final int MAX_POR_IP = 20;
    static final Duration JANELA = Duration.ofMinutes(15);
    private static final int MAX_CHAVES = 10_000;

    private record Contador(int falhas, Instant inicio) {
    }

    private final ConcurrentHashMap<String, Contador> contadores = new ConcurrentHashMap<>();

    public void verificar(String email, String ip) {
        if (excedeu("e:" + email, MAX_POR_EMAIL) || excedeu("i:" + ip, MAX_POR_IP)) {
            throw new TentativasExcedidasException();
        }
    }

    public void registrarFalha(String email, String ip) {
        limparSeNecessario();
        incrementar("e:" + email);
        incrementar("i:" + ip);
    }

    public void registrarSucesso(String email) {
        contadores.remove("e:" + email);
    }

    private boolean excedeu(String chave, int max) {
        Contador c = contadores.get(chave);
        return c != null && !expirou(c) && c.falhas() >= max;
    }

    private void incrementar(String chave) {
        contadores.merge(chave, new Contador(1, Instant.now()), (atual, novo) ->
                expirou(atual) ? novo : new Contador(atual.falhas() + 1, atual.inicio()));
    }

    private boolean expirou(Contador c) {
        return c.inicio().plus(JANELA).isBefore(Instant.now());
    }

    /** Impede crescimento ilimitado do mapa por chaves forjadas. */
    private void limparSeNecessario() {
        if (contadores.size() > MAX_CHAVES) {
            contadores.values().removeIf(this::expirou);
        }
    }
}
