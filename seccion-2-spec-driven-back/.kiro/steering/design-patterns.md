---
inclusion: always
---

# Patrones de Diseño

Este documento describe los patrones de diseño obligatorios en el proyecto. Aplícalos consistentemente en cada nueva funcionalidad.

## Patrones y su aplicación

### Strategy — Cálculo de recargo por urgencia

- **Clase principal:** `UrgencyPricingStrategy` (interfaz en `domain`)
- **Propósito:** Encapsular el algoritmo de recargo según el nivel de urgencia del pedido.
- **Regla:** Cada nivel de urgencia es una implementación concreta de la estrategia. Para agregar un nuevo nivel, crea una nueva implementación; nunca modifiques las existentes (principio abierto/cerrado).

### Factory Method — Creación de `Cotizacion`

- **Método:** método estático de fábrica en la propia clase `Cotizacion` (e.g., `Cotizacion.crear(...)`)
- **Propósito:** Validar invariantes de negocio (RN-04, RN-05) en el momento de construcción.
- **Regla:** No uses constructores públicos directamente para instanciar `Cotizacion`. Toda construcción pasa por el factory method, garantizando que el objeto nunca exista en estado inválido.

### Repository — Puertos de salida de persistencia

- **Puertos:** `CotizacionRepositoryPort`, `CalzadoRepositoryPort`, `ReparacionRepositoryPort`
- **Propósito:** Aislar el dominio de la tecnología de persistencia concreta (en memoria, JPA, etc.).
- **Regla:** Los puertos se definen en `application.port.out`. Las implementaciones (`InMemoryAdapter`, `JpaAdapter`) viven en `infrastructure`. El dominio nunca importa implementaciones concretas.

### DTO + Mapper — Contrato HTTP desacoplado del dominio

- **Clases:** `CotizacionRequest`, `CotizacionResponse`, `CotizacionMapper`
- **Propósito:** Evitar que el modelo de dominio quede expuesto o acoplado al contrato HTTP.
- **Regla:** Los DTOs y el mapper viven exclusivamente en `infrastructure.adapter.in.rest`. El mapper es el único punto de conversión entre DTO y entidad de dominio; nunca hagas esa conversión en el controlador ni en el servicio.

### Inyección de dependencias — Servicios de aplicación

- **Propósito:** Invertir el control entre `application` e `infrastructure`, cumpliendo la regla de dependencia hexagonal.
- **Regla:** Los servicios (`*Service`) reciben sus puertos de salida (`*RepositoryPort`) por constructor. No instancies repositorios ni adaptadores directamente dentro de un servicio. La configuración (wiring) ocurre en la capa de `infrastructure` (e.g., clases `@Configuration` de Spring).

## Checklist al implementar una nueva funcionalidad

1. ¿El recargo por urgencia usa una implementación de `UrgencyPricingStrategy`?
2. ¿Las instancias de `Cotizacion` se crean exclusivamente vía el factory method?
3. ¿Los accesos a datos pasan por un `*RepositoryPort`, no por un repositorio JPA directo?
4. ¿Los DTOs y el mapper están en `infrastructure.adapter.in.rest`?
5. ¿Los servicios reciben sus dependencias por constructor (inyección), sin instanciarlas internamente?
