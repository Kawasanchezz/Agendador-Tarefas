package com.agendador.bff.controller;

import com.agendador.bff.client.ViaCepClient;
import com.agendador.bff.dto.Dtos.EnderecoCepResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/endereco")
@RequiredArgsConstructor
@Validated
@Tag(name = "CEP", description = "Consulta de endereço via ViaCEP")
public class EnderecoController {

    private final ViaCepClient viaCep;

    @GetMapping("/{cep}")
    @Operation(summary = "Buscar endereço pelo CEP (8 dígitos)")
    public EnderecoCepResponse buscar(
            @PathVariable @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String cep) {
        return viaCep.buscar(cep);
    }
}
