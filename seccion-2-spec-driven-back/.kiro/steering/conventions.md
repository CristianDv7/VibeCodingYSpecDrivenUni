---
inclusion: always
---

# Convenciones de Nomenclatura

Este proyecto es un backend Java con arquitectura hexagonal. El paquete raíz es `com.tallerdae.cotizador`. Todas las clases nuevas deben respetar estrictamente las convenciones de nombres que se describen a continuación.

## Reglas generales

- **Paquete raíz:** `com.tallerdae.cotizador` — minúsculas, notación de dominio invertido.
- **Métodos:** camelCase, verbo que expresa intención clara (e.g., `calcularTotal()`, `generarCotizacion()`).
- **Constantes:** `MAYÚSCULAS_CON_GUION_BAJO` (e.g., `RECARGO_URGENCIA_PORCENTAJE`).

## Nomenclatura por tipo de artefacto

| Tipo de artefacto | Convención | Ejemplo |
|---|---|---|
| Entidad / Value Object de dominio | PascalCase, sustantivo, **sin sufijo técnico** | `Cotizacion`, `Calzado`, `Dinero` |
| Puerto de entrada (caso de uso) | PascalCase + sufijo `UseCase` | `GenerarCotizacionUseCase` |
| Puerto de salida (repositorio) | PascalCase + sufijo `RepositoryPort` | `CotizacionRepositoryPort` |
| Implementación de caso de uso | PascalCase + sufijo `Service` | `GenerarCotizacionService` |
| Adaptador REST de entrada | PascalCase + sufijo `Controller` | `CotizacionController` |
| Adaptador de persistencia | PascalCase + sufijo `InMemoryAdapter` o `JpaAdapter` | `InMemoryCotizacionRepositoryAdapter` |
| DTO de entrada HTTP | PascalCase + sufijo `Request` | `CotizacionRequest` |
| DTO de salida HTTP | PascalCase + sufijo `Response` | `CotizacionResponse` |
| Mapper dominio ↔ DTO | PascalCase + sufijo `Mapper` | `CotizacionMapper` |

## Errores comunes a evitar

- No agregues sufijos técnicos (`Entity`, `Model`, `Impl`) a entidades o Value Objects de dominio.
- No uses el sufijo `Repository` directamente — los puertos de salida usan `RepositoryPort` y los adaptadores usan `InMemoryAdapter` o `JpaAdapter`.
- No crees clases con nombres genéricos como `Helper`, `Manager`, `Utils` sin justificación explícita.
