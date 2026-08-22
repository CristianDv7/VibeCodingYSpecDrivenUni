package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerdae.cotizador.application.port.in.ListarCalzadosUseCase;

@RestController
@RequestMapping("/calzados")
public class CalzadoController {

    private final ListarCalzadosUseCase listarCalzadosUseCase;

    public CalzadoController(ListarCalzadosUseCase listarCalzadosUseCase) {
        this.listarCalzadosUseCase = listarCalzadosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CalzadoResponse>> listar() {
        List<CalzadoResponse> respuesta = listarCalzadosUseCase.listar()
                .stream()
                .map(CotizacionMapper::toCalzadoResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}
