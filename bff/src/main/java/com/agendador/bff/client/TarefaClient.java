package com.agendador.bff.client;

import com.agendador.bff.dto.Dtos.StatusTarefa;
import com.agendador.bff.dto.Dtos.TarefaRequest;
import com.agendador.bff.dto.Dtos.TarefaResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TarefaClient {

    private static final String HEADER = "X-Usuario-Email";

    private final RestClient http;

    public TarefaClient(@Qualifier("tarefasRestClient") RestClient http) {
        this.http = http;
    }

    public TarefaResponse criar(String email, TarefaRequest req) {
        return http.post().uri("/tarefas").header(HEADER, email).body(req)
                .retrieve().body(TarefaResponse.class);
    }

    public List<TarefaResponse> listar(String email, LocalDateTime inicial, LocalDateTime fim) {
        return http.get().uri(u -> {
                    u.path("/tarefas");
                    if (inicial != null) u.queryParam("dataInicial", inicial);
                    if (fim != null) u.queryParam("dataFinal", fim);
                    return u.build();
                })
                .header(HEADER, email).retrieve().body(new ParameterizedTypeReference<>() {
                });
    }

    public TarefaResponse detalhar(String email, String id) {
        return http.get().uri(u -> u.path("/tarefas/{id}").build(id)).header(HEADER, email)
                .retrieve().body(TarefaResponse.class);
    }

    public TarefaResponse alterarStatus(String email, String id, StatusTarefa status) {
        return http.patch().uri(u -> u.path("/tarefas").queryParam("id", id)
                        .queryParam("status", status).build())
                .header(HEADER, email).retrieve().body(TarefaResponse.class);
    }

    public void deletar(String email, String id) {
        http.delete().uri(u -> u.path("/tarefas").queryParam("id", id).build())
                .header(HEADER, email).retrieve().toBodilessEntity();
    }
}
