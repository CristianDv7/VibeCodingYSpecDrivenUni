package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

public class CotizacionRequest {

    private UUID calzadoId;
    private List<UUID> reparacionIds;
    private boolean esUrgente;

    public CotizacionRequest() {
    }

    public UUID getCalzadoId() {
        return calzadoId;
    }

    public void setCalzadoId(UUID calzadoId) {
        this.calzadoId = calzadoId;
    }

    public List<UUID> getReparacionIds() {
        return reparacionIds;
    }

    public void setReparacionIds(List<UUID> reparacionIds) {
        this.reparacionIds = reparacionIds;
    }

    public boolean isEsUrgente() {
        return esUrgente;
    }

    public void setEsUrgente(boolean esUrgente) {
        this.esUrgente = esUrgente;
    }
}
