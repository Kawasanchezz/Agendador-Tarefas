package com.agendador.bff.client;

import com.agendador.bff.dto.Dtos.EnderecoCepResponse;
import com.agendador.bff.exception.Excecoes.CepNaoEncontradoException;
import com.agendador.bff.exception.Excecoes.ServicoExternoException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ViaCepClient {

    private final RestClient http;

    public ViaCepClient(@Qualifier("viaCepRestClient") RestClient http) {
        this.http = http;
    }

    public EnderecoCepResponse buscar(String cep) {
        JsonNode n;
        try {
            n = http.get().uri("/{cep}/json/", cep).retrieve().body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ServicoExternoException("Falha ao consultar o ViaCEP", e);
        }
        if (n == null || n.has("erro")) {
            throw new CepNaoEncontradoException(cep);
        }
        return new EnderecoCepResponse(cep, texto(n, "logradouro"), texto(n, "complemento"),
                texto(n, "bairro"), texto(n, "localidade"), texto(n, "uf"));
    }

    private String texto(JsonNode n, String campo) {
        return n.hasNonNull(campo) ? n.get(campo).asText() : null;
    }
}
