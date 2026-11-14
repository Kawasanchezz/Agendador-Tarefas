package com.agendador.tarefas.controller;

import com.agendador.tarefas.domain.StatusTarefa;
import com.agendador.tarefas.dto.Dtos.TarefaRequest;
import com.agendador.tarefas.dto.Dtos.TarefaResponse;
import com.agendador.tarefas.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API interna: só o BFF a chama. O e-mail do dono vem no header X-Usuario-Email.
 */
@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private static final String HEADER = "X-Usuario-Email";

    private final TarefaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(@RequestHeader(HEADER) String email, @Valid @RequestBody TarefaRequest req) {
        return service.criar(email, req);
    }

    @GetMapping
    public List<TarefaResponse> listar(
            @RequestHeader(HEADER) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal) {
        return service.listar(email, dataInicial, dataFinal);
    }

    @GetMapping("/{id}")
    public TarefaResponse detalhar(@RequestHeader(HEADER) String email, @PathVariable String id) {
        return service.detalhar(email, id);
    }

    @PatchMapping
    public TarefaResponse alterarStatus(@RequestHeader(HEADER) String email, @RequestParam String id,
                                        @RequestParam StatusTarefa status) {
        return service.alterarStatus(email, id, status);
    }

    @DeleteMapping("/usuario")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarTodas(@RequestHeader(HEADER) String email) {
        service.deletarTodas(email);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@RequestHeader(HEADER) String email, @RequestParam String id) {
        service.deletar(email, id);
    }
}
