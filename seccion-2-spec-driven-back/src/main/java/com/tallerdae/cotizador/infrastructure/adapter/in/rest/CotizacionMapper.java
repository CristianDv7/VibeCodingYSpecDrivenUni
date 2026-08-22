package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import com.tallerdae.cotizador.domain.Calzado;
import com.tallerdae.cotizador.domain.Cotizacion;
import com.tallerdae.cotizador.domain.Reparacion;

public final class CotizacionMapper {

    private CotizacionMapper() {
    }

    public static CotizacionResponse toResponse(Cotizacion cotizacion) {
        return new CotizacionResponse(
                cotizacion.getId(),
                cotizacion.getFechaCreacion(),
                cotizacion.getSubtotal(),
                cotizacion.getRecargoUrgencia(),
                cotizacion.getTotal(),
                cotizacion.getTiempoEstimadoDias()
        );
    }

    public static CalzadoResponse toCalzadoResponse(Calzado calzado) {
        return new CalzadoResponse(
                calzado.getId(),
                calzado.getNombre(),
                calzado.getFactorComplejidad()
        );
    }

    public static ReparacionResponse toReparacionResponse(Reparacion reparacion) {
        return new ReparacionResponse(
                reparacion.getId(),
                reparacion.getNombre(),
                reparacion.getPrecioBase(),
                reparacion.getTiempoEstimadoDias()
        );
    }
}
