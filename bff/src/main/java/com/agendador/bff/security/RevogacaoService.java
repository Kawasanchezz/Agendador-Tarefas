package com.agendador.bff.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Invalida tokens emitidos até o momento da revogação (logout, troca de senha, exclusão de conta).
 * Fica em memória: reiniciar o BFF ou ter várias instâncias exige um armazenamento compartilhado (Redis).
 */
@Component
public class RevogacaoService {

    private final long validadeTokenMs;
    private final ConcurrentHashMap<String, Instant> revogadoEm = new ConcurrentHashMap<>();

    public RevogacaoService(@Value("${jwt.expiration-ms}") long validadeTokenMs) {
        this.validadeTokenMs = validadeTokenMs;
    }

    public void revogarTodos(String email) {
        Instant agora = Instant.now();
        // entradas mais antigas que a validade de um token não têm mais o que invalidar
        Instant limite = agora.minusMillis(validadeTokenMs);
        revogadoEm.values().removeIf(t -> t.isBefore(limite));
        revogadoEm.put(email, agora);
    }

    /** O JWT guarda o instante de emissão em segundos, então comparamos em segundos (lado seguro: revoga). */
    public boolean estaRevogado(String email, Instant emitidoEm) {
        Instant r = revogadoEm.get(email);
        return r != null && !emitidoEm.isAfter(r.truncatedTo(ChronoUnit.SECONDS));
    }
}
