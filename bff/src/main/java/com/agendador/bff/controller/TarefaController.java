package com.agendador.bff.controller;

import com.agendador.bff.client.TarefaClient;
import com.agendador.bff.dto.Dtos.StatusTarefa;
import com.agendador.bff.dto.Dtos.TarefaRequest;
import com.agendador.bff.dto.Dtos.TarefaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Tarefas do usuário autenticado")
public class TarefaController {

    private final TarefaClient tarefas;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar tarefa (status inicial PENDENTE)")
    public TarefaResponse criar(@AuthenticationPrincipal String email, @Valid @RequestBody TarefaRequest req) {
        return tarefas.criar(email, req);
    }

    @GetMapping
    @Operation(summary = "Listar tarefas, com filtro opcional por período da data do evento")
    public List<TarefaResponse> listar(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal) {
        return tarefas.listar(email, dataInicial, dataFinal);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar tarefa")
    public TarefaResponse detalhar(@AuthenticationPrincipal String email, @PathVariable String id) {
        return tarefas.detalhar(email, id);
    }

    @PatchMapping
    @Operation(summary = "Alterar status da tarefa")
    public TarefaResponse alterarStatus(@AuthenticationPrincipal String email, @RequestParam String id,
                                        @RequestParam StatusTarefa status) {
        return tarefas.alterarStatus(email, id, status);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir tarefa")
    public void deletar(@AuthenticationPrincipal String email, @RequestParam String id) {
        tarefas.deletar(email, id);
    }
}
