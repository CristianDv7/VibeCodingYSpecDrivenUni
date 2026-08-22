package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tallerdae.cotizador.domain.CalzadoNotFoundException;
import com.tallerdae.cotizador.domain.CotizacionNotFoundException;
import com.tallerdae.cotizador.domain.ReparacionNotFoundException;
import com.tallerdae.cotizador.domain.ReparacionesVaciasException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReparacionesVaciasException.class)
    public ResponseEntity<Map<String, Object>> handleReparacionesVacias(
            ReparacionesVaciasException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(CalzadoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCalzadoNotFound(
            CalzadoNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ReparacionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReparacionNotFound(
            ReparacionNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CotizacionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCotizacionNotFound(
            CotizacionNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
