package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerdae.cotizador.application.port.in.ListarReparacionesUseCase;

@RestController
@RequestMapping("/reparaciones")
public class ReparacionController {

    private final ListarReparacionesUseCase listarReparacionesUseCase;

    public ReparacionController(ListarReparacionesUseCase listarReparacionesUseCase) {
        this.listarReparacionesUseCase = listarReparacionesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ReparacionResponse>> listar() {
        List<ReparacionResponse> respuesta = listarReparacionesUseCase.listar()
                .stream()
                .map(CotizacionMapper::toReparacionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }
}
