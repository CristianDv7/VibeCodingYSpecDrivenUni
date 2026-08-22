# Implementation Plan: Cotizador de Reparación de Calzado

## Overview

Plan de implementación incremental del backend REST con arquitectura hexagonal en Spring Boot.
Las tareas avanzan desde el núcleo de dominio hacia afuera: `domain` → `application` → `infrastructure`.
Cada paso produce código integrable y verificable antes de continuar con el siguiente.

Paquete raíz: `com.tallerdae.cotizador`.
PBT: biblioteca **jqwik** integrada con JUnit 5.

---

## Tasks

- [x] 1. Configurar el proyecto Spring Boot y la estructura de paquetes
  - Crear el proyecto Maven con Spring Boot (`spring-boot-starter-web`) y dependencias base.
  - Agregar la dependencia de jqwik (`net.jqwik:jqwik:1.8.x`, scope test) en el `pom.xml`.
  - Crear la estructura de paquetes vacíos bajo `com.tallerdae.cotizador`:
    - `domain`
    - `application.port.in`
    - `application.port.out`
    - `application.service`
    - `infrastructure.adapter.in.rest`
    - `infrastructure.adapter.out.persistence`
    - `infrastructure.config`
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 2. Implementar la capa de dominio
  - [x] 2.1 Crear el Value Object `Calzado`
    - Campos: `UUID id`, `String nombre`, `BigDecimal factorComplejidad`.
    - Clase inmutable con constructor completo. Sin anotaciones de framework.
    - _Requirements: 1.2_

  - [x] 2.2 Crear el Value Object `Reparacion`
    - Campos: `UUID id`, `String nombre`, `BigDecimal precioBase`, `int tiempoEstimadoDias`.
    - Clase inmutable con constructor completo. Sin anotaciones de framework.
    - _Requirements: 2.2_

  - [x] 2.3 Crear la interfaz `UrgencyPricingStrategy`
    - Métodos: `calcularRecargo(BigDecimal subtotal)`, `calcularTotal(BigDecimal subtotal, BigDecimal recargo)`, `calcularTiempoEntrega(List<Reparacion> reparaciones)`.
    - Reside en `domain`. Sin anotaciones de framework.
    - _Requirements: 3.2, 4.1_

  - [x] 2.4 Crear `NonUrgentPricingStrategy`
    - Implementa `UrgencyPricingStrategy`.
    - `calcularRecargo` → `BigDecimal.ZERO`; `calcularTotal` → `subtotal`; `calcularTiempoEntrega` → `max(tiempoEstimadoDias)`.
    - _Requirements: 3.2, 3.3_

  - [x] 2.5 Crear `UrgentPricingStrategy`
    - Implementa `UrgencyPricingStrategy`.
    - `calcularRecargo` → `subtotal × 0.30` (escala 2, `HALF_UP`); `calcularTotal` → `subtotal + recargo`; `calcularTiempoEntrega` → `max(ceil(max_dias / 2.0), 1)`.
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 2.6 Crear `CalzadoNotFoundException`
    - Extiende `RuntimeException`. Recibe `UUID id` en el constructor.
    - Mensaje de error fijo definido como constante dentro de la clase.
    - _Requirements: 3.7_

  - [x] 2.7 Crear `ReparacionNotFoundException`
    - Extiende `RuntimeException`. Recibe `UUID id` en el constructor.
    - Mensaje de error fijo definido como constante dentro de la clase.
    - _Requirements: 3.8_

  - [x] 2.8 Crear `ReparacionesVaciasException`
    - Extiende `RuntimeException`.
    - Mensaje de error fijo definido como constante dentro de la clase.
    - _Requirements: 3.6_

  - [x] 2.9 Crear `CotizacionNotFoundException`
    - Extiende `RuntimeException`. Recibe `UUID id` en el constructor.
    - Mensaje de error fijo definido como constante dentro de la clase.
    - _Requirements: 3.8_

  - [x] 2.10 Crear la entidad `Cotizacion` con el factory method estático `Cotizacion.crear(...)`
    - Campos: `UUID id`, `LocalDateTime fechaCreacion`, `Calzado calzado`, `List<Reparacion> reparaciones`, `boolean esUrgente`, `BigDecimal subtotal`, `BigDecimal recargoUrgencia`, `BigDecimal total`, `int tiempoEstimadoDias`.
    - Constructor privado; instanciación exclusiva vía `Cotizacion.crear(Calzado, List<Reparacion>, boolean, UrgencyPricingStrategy)`.
    - El factory method debe: validar lista no vacía/nula (lanza `ReparacionesVaciasException`), generar `UUID.randomUUID()` y `LocalDateTime.now()`, calcular `subtotal = Σ(precioBase_i × factorComplejidad)` con escala 2 `HALF_UP`, delegar recargo/total/tiempo a `strategy`.
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6_

  - [x] 2.11 Escribir pruebas unitarias de `Cotizacion` (factory method)
    - Verificar que `Cotizacion.crear(...)` lanza `ReparacionesVaciasException` con lista vacía.
    - Verificar que `Cotizacion.crear(...)` lanza `ReparacionesVaciasException` con lista nula.
    - Verificar que con entrada válida el UUID y la `fechaCreacion` son no nulos.
    - _Requirements: 3.1, 3.4, 3.6_

  - [x] 2.12 Escribir pruebas unitarias de `UrgentPricingStrategy`
    - Verificar el recargo con valores decimales concretos (ej. subtotal = 48.00 → recargo = 14.40).
    - Verificar que `calcularTotal` = subtotal + recargo.
    - Verificar que `calcularTiempoEntrega` aplica techo de la mitad con mínimo de 1 día.
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [ ] 2.13 Escribir pruebas unitarias de `NonUrgentPricingStrategy`
    - Verificar que el recargo es cero.
    - Verificar que `calcularTotal` = subtotal.
    - Verificar que `calcularTiempoEntrega` = máximo de los tiempos de la lista.
    - _Requirements: 3.2, 3.3_

- [ ] 3. Implementar los puertos y servicios de aplicación
  - [x] 3.1 Crear `GenerarCotizacionUseCase`
    - Interfaz en `application.port.in`.
    - Método: `generar(UUID calzadoId, List<UUID> reparacionIds, boolean esUrgente): Cotizacion`.
    - _Requirements: 3.1_

  - [x] 3.2 Crear `ListarCalzadosUseCase`
    - Interfaz en `application.port.in`.
    - Método: `listar(): List<Calzado>`.
    - _Requirements: 1.1_

  - [x] 3.3 Crear `ListarReparacionesUseCase`
    - Interfaz en `application.port.in`.
    - Método: `listar(): List<Reparacion>`.
    - _Requirements: 2.1_

  - [x] 3.4 Crear `CalzadoRepositoryPort`
    - Interfaz en `application.port.out`.
    - Métodos: `findAll(): List<Calzado>`, `findById(UUID id): Optional<Calzado>`.
    - _Requirements: 1.1_

  - [x] 3.5 Crear `ReparacionRepositoryPort`
    - Interfaz en `application.port.out`.
    - Métodos: `findAll(): List<Reparacion>`, `findById(UUID id): Optional<Reparacion>`, `findAllById(List<UUID> ids): List<Reparacion>`.
    - _Requirements: 2.1_

  - [x] 3.6 Crear `CotizacionRepositoryPort`
    - Interfaz en `application.port.out`.
    - Métodos: `save(Cotizacion cotizacion): Cotizacion`, `findById(UUID id): Optional<Cotizacion>`, `findAll(): List<Cotizacion>`.
    - _Requirements: 3.1_

  - [x] 3.7 Implementar `ListarCalzadosService`
    - Implementa `ListarCalzadosUseCase`. Inyecta `CalzadoRepositoryPort` por constructor.
    - Delega en `port.findAll()`. Sin anotaciones Spring.
    - _Requirements: 1.1_

  - [x] 3.8 Implementar `ListarReparacionesService`
    - Implementa `ListarReparacionesUseCase`. Inyecta `ReparacionRepositoryPort` por constructor.
    - Delega en `port.findAll()`. Sin anotaciones Spring.
    - _Requirements: 2.1_

  - [x] 3.9 Implementar `GenerarCotizacionService`
    - Implementa `GenerarCotizacionUseCase`. Inyecta `CalzadoRepositoryPort`, `ReparacionRepositoryPort`, `CotizacionRepositoryPort` por constructor.
    - Flujo: `findById(calzadoId)` → lanza `CalzadoNotFoundException` si vacío; `findAllById(reparacionIds)` → lanza `ReparacionNotFoundException` para cada ID no encontrado; selecciona `UrgentPricingStrategy` o `NonUrgentPricingStrategy` según `esUrgente`; invoca `Cotizacion.crear(...)`; persiste con `save`; retorna la cotización.
    - Sin anotaciones Spring.
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 4.1, 4.2, 4.3, 4.4, 4.5_

  - [ ]* 3.10 Escribir pruebas unitarias de `GenerarCotizacionService`
    - Flujo nominal con mocks de los tres `RepositoryPort`.
    - Verificar que lanza `CalzadoNotFoundException` cuando el repositorio retorna `Optional.empty()`.
    - Verificar que lanza `ReparacionNotFoundException` cuando algún ID no existe.
    - _Requirements: 3.1, 3.7, 3.8_

  - [ ]* 3.11 Escribir pruebas unitarias de `ListarCalzadosService`
    - Verificar delegación a `CalzadoRepositoryPort.findAll()`.
    - _Requirements: 1.1_

  - [ ]* 3.12 Escribir pruebas unitarias de `ListarReparacionesService`
    - Verificar delegación a `ReparacionRepositoryPort.findAll()`.
    - _Requirements: 2.1_

- [x] 4. Punto de control — verificar compilación y pruebas de dominio/aplicación
  - Asegurarse de que el proyecto compila sin errores y todas las pruebas escritas hasta este punto pasan. Consultar al usuario si hay dudas antes de continuar.

- [ ] 5. Implementar los adaptadores de persistencia en memoria
  - [x] 5.1 Implementar `InMemoryCalzadoRepositoryAdapter`
    - Implementa `CalzadoRepositoryPort`.
    - Mantiene un `Map<UUID, Calzado>` inicializado en el constructor con al menos 3 instancias de ejemplo (e.g., "Zapatilla Deportiva" factor 1.2, "Bota de Cuero" factor 1.5, "Sandalia" factor 0.8).
    - Implementa `findAll()` y `findById(UUID)`. Sin anotaciones Spring.
    - _Requirements: 1.1, 1.2_

  - [x] 5.2 Implementar `InMemoryReparacionRepositoryAdapter`
    - Implementa `ReparacionRepositoryPort`.
    - Mantiene un `Map<UUID, Reparacion>` con al menos 3 reparaciones de ejemplo (e.g., "Cambio de Suela" precio 25.00 días 3, "Costura Lateral" precio 15.00 días 2, "Limpieza Profunda" precio 10.00 días 1).
    - Implementa `findAll()`, `findById(UUID)` y `findAllById(List<UUID>)`. Sin anotaciones Spring.
    - _Requirements: 2.1, 2.2_

  - [x] 5.3 Implementar `InMemoryCotizacionRepositoryAdapter`
    - Implementa `CotizacionRepositoryPort`.
    - Mantiene un `Map<UUID, Cotizacion>` en memoria, inicialmente vacío.
    - Implementa `save(Cotizacion)`, `findById(UUID)` y `findAll()`. Sin anotaciones Spring.
    - _Requirements: 3.4, 3.5_

  - [ ]* 5.4 Escribir pruebas unitarias de `InMemoryCalzadoRepositoryAdapter`
    - Verificar que `findAll()` retorna todos los elementos del mapa inicial.
    - Verificar que `findById` retorna `Optional.empty()` para un ID inexistente.
    - _Requirements: 1.1_

  - [ ]* 5.5 Escribir pruebas unitarias de `InMemoryReparacionRepositoryAdapter`
    - Verificar que `findAllById` con una lista parcialmente inválida retorna solo los existentes.
    - _Requirements: 2.1_

  - [ ]* 5.6 Escribir pruebas unitarias de `InMemoryCotizacionRepositoryAdapter`
    - Verificar que `save` persiste la cotización y `findById` la recupera correctamente.
    - _Requirements: 3.4_

- [ ] 6. Implementar la capa REST y el wiring de Spring
  - [x] 6.1 Crear `CotizacionRequest`
    - DTO de entrada en `infrastructure.adapter.in.rest`.
    - Campos: `UUID calzadoId`, `List<UUID> reparacionIds`, `boolean esUrgente`.
    - _Requirements: 3.1_

  - [x] 6.2 Crear `CotizacionResponse`
    - DTO de salida en `infrastructure.adapter.in.rest`.
    - Campos: `UUID id`, `LocalDateTime fechaCreacion`, `BigDecimal subtotal`, `BigDecimal recargoUrgencia`, `BigDecimal total`, `int tiempoEstimadoDias`.
    - _Requirements: 3.5, 4.5_

  - [x] 6.3 Crear `CalzadoResponse`
    - DTO de salida en `infrastructure.adapter.in.rest`.
    - Campos: `UUID id`, `String nombre`, `BigDecimal factorComplejidad`.
    - _Requirements: 1.2_

  - [x] 6.4 Crear `ReparacionResponse`
    - DTO de salida en `infrastructure.adapter.in.rest`.
    - Campos: `UUID id`, `String nombre`, `BigDecimal precioBase`, `int tiempoEstimadoDias`.
    - _Requirements: 2.2_

  - [x] 6.5 Crear `CotizacionMapper`
    - Reside en `infrastructure.adapter.in.rest`.
    - Métodos: `toResponse(Cotizacion)`, `toCalzadoResponse(Calzado)`, `toReparacionResponse(Reparacion)`.
    - Es el único punto de conversión entre entidades de dominio y DTOs.
    - _Requirements: 1.2, 2.2, 3.5, 4.5_

  - [x] 6.6 Implementar `CalzadoController`
    - `@RestController` en `infrastructure.adapter.in.rest`.
    - `GET /calzados` → llama `ListarCalzadosUseCase.listar()` → mapea con `CotizacionMapper.toCalzadoResponse` → retorna `ResponseEntity<List<CalzadoResponse>>` con HTTP 200.
    - Recibe `ListarCalzadosUseCase` por constructor.
    - _Requirements: 1.1, 1.2_

  - [x] 6.7 Implementar `ReparacionController`
    - `@RestController` en `infrastructure.adapter.in.rest`.
    - `GET /reparaciones` → llama `ListarReparacionesUseCase.listar()` → mapea con `CotizacionMapper.toReparacionResponse` → retorna `ResponseEntity<List<ReparacionResponse>>` con HTTP 200.
    - Recibe `ListarReparacionesUseCase` por constructor.
    - _Requirements: 2.1, 2.2_

  - [x] 6.8 Implementar `CotizacionController`
    - `@RestController` en `infrastructure.adapter.in.rest`.
    - `POST /cotizaciones` con `@RequestBody CotizacionRequest`.
    - Llama `GenerarCotizacionUseCase.generar(request.calzadoId, request.reparacionIds, request.esUrgente)`.
    - Mapea el resultado con `CotizacionMapper.toResponse`. Retorna `ResponseEntity<CotizacionResponse>` con HTTP 201.
    - Recibe `GenerarCotizacionUseCase` por constructor.
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 6.9 Implementar `GlobalExceptionHandler`
    - `@RestControllerAdvice` en `infrastructure.adapter.in.rest`.
    - Mapea: `ReparacionesVaciasException` → 400, `CalzadoNotFoundException` → 404, `ReparacionNotFoundException` → 404, `CotizacionNotFoundException` → 404, cualquier `Exception` no controlada → 500.
    - Formato de respuesta: `{ timestamp, status, error, message, path }`.
    - _Requirements: 3.6, 3.7, 3.8_

  - [x] 6.10 Implementar `CotizadorConfiguration`
    - `@Configuration` en `infrastructure.config`.
    - Declara los beans de los tres adaptadores en memoria y los tres servicios, inyectando los puertos por constructor.
    - Es el único punto de wiring entre servicios y adaptadores.
    - _Requirements: 3.1, 3.6, 3.7, 3.8_

  - [ ]* 6.11 Escribir pruebas de `CotizacionMapper`
    - Verificar conversión `Cotizacion` → `CotizacionResponse` con ejemplo concreto.
    - Verificar conversión `Calzado` → `CalzadoResponse`.
    - Verificar conversión `Reparacion` → `ReparacionResponse`.
    - _Requirements: 1.2, 2.2, 3.5_

  - [ ]* 6.12 Escribir pruebas `@WebMvcTest` de `CalzadoController`
    - Verificar que `GET /calzados` retorna HTTP 200 y cuerpo JSON correcto.
    - _Requirements: 1.1, 1.2_

  - [ ]* 6.13 Escribir pruebas `@WebMvcTest` de `ReparacionController`
    - Verificar que `GET /reparaciones` retorna HTTP 200 y cuerpo JSON correcto.
    - _Requirements: 2.1, 2.2_

  - [ ]* 6.14 Escribir pruebas `@WebMvcTest` de `CotizacionController`
    - Verificar que `POST /cotizaciones` retorna HTTP 201 con respuesta correcta.
    - _Requirements: 3.1, 3.5, 4.5_

  - [ ]* 6.15 Escribir pruebas de `GlobalExceptionHandler` (mapeo de excepciones a HTTP)
    - Verificar que `ReparacionesVaciasException` → 400.
    - Verificar que `CalzadoNotFoundException` → 404.
    - Verificar que `ReparacionNotFoundException` → 404.
    - _Requirements: 3.6, 3.7, 3.8_

- [x] 7. Punto de control — verificar integración completa
  - Asegurarse de que el proyecto compila, el contexto Spring arranca correctamente y todos los tests pasan. Consultar al usuario si hay dudas antes de continuar.

- [ ] 8. Implementar las pruebas de propiedad con jqwik
  - [x] 8.1 Implementar Property 1 — estructura completa de `Calzado` retornado
    - **Propiedad 1: `calzadoRetornadoTieneEstructuraCompleta`**
    - Para cualquier lista de `Calzado` en el repositorio en memoria, `listar()` retorna objetos con `id` no nulo, `nombre` no nulo y `factorComplejidad > 0`.
    - **Valida: Requisito 1.2**

  - [x] 8.2 Implementar Property 2 — estructura completa de `Reparacion` retornada
    - **Propiedad 2: `reparacionRetornadaTieneEstructuraCompleta`**
    - Para cualquier lista de `Reparacion` en el repositorio en memoria, `listar()` retorna objetos con `id` no nulo, `nombre` no nulo, `precioBase > 0` y `tiempoEstimadoDias >= 1`.
    - **Valida: Requisito 2.2**

  - [x] 8.3 Implementar Property 3 — cálculo correcto del subtotal (RN-01)
    - **Propiedad 3: `subtotalEsSumaProductos`**
    - Para cualquier `Calzado` con `factorComplejidad > 0` y lista no vacía de `Reparacion` con `precioBase > 0`, el `subtotal` de la cotización debe ser igual a `Σ(precioBase_i × factorComplejidad)`.
    - **Valida: Requisito 3.1**

  - [x] 8.4 Implementar Property 4 — cotización no urgente sin recargo (RN-01, RN-02)
    - **Propiedad 4: `cotizacionNoUrgenteRecargoEsCero`**
    - Para cualquier cotización con `esUrgente = false`, `recargoUrgencia == 0` y `total == subtotal`.
    - **Valida: Requisito 3.2**

  - [x] 8.5 Implementar Property 5 — tiempo no urgente es el máximo (RN-03)
    - **Propiedad 5: `tiempoNoUrgenteEsMaximo`**
    - Para cualquier lista de reparaciones y cotización con `esUrgente = false`, `tiempoEstimadoDias == max(reparacion.tiempoEstimadoDias)`.
    - **Valida: Requisito 3.3**

  - [x] 8.6 Implementar Property 6 — unicidad de identificadores de `Cotizacion` (RN-05)
    - **Propiedad 6: `cadaCotizacionTieneIdUnico`**
    - Para N cotizaciones generadas con entradas válidas, cada una tiene `id` no nulo, `fechaCreacion` no nula y ningún par comparte el mismo `id`.
    - **Valida: Requisito 3.4**

  - [x] 8.7 Implementar Property 7 — rechazo de cotización con reparaciones vacías (RN-04)
    - **Propiedad 7: `listaVaciaLanzaExcepcion`**
    - Para cualquier solicitud con lista de reparaciones nula o vacía, `Cotizacion.crear(...)` lanza `ReparacionesVaciasException` y no se crea ninguna instancia.
    - **Valida: Requisito 3.6**

  - [x] 8.8 Implementar Property 8 — rechazo de calzado o reparación inexistente
    - **Propiedad 8: `idInexistenteLanzaExcepcion`**
    - Para cualquier UUID no registrado como calzado, el servicio lanza `CalzadoNotFoundException`. Para cualquier lista con al menos un ID de reparación no registrado, lanza `ReparacionNotFoundException` con el ID inválido.
    - **Valida: Requisitos 3.7, 3.8**

  - [x] 8.9 Implementar Property 9 — cotización urgente: recargo e invariante total (RN-02)
    - **Propiedad 9: `cotizacionUrgenteRecargoEsTreintaPorciento`**
    - Para cualquier subtotal positivo con `esUrgente = true`, `recargoUrgencia == subtotal × 0.30` y `total == subtotal + recargoUrgencia`.
    - **Valida: Requisitos 4.1, 4.2**

  - [x] 8.10 Implementar Property 10 — cotización urgente: tiempo reducido con mínimo 1 día (RN-03)
    - **Propiedad 10: `tiempoUrgenteEsTechoMitadConMinimo`**
    - Para cualquier lista de reparaciones con `tiempoEstimadoDias >= 1` y cotización con `esUrgente = true`, `tiempoEstimadoDias == max(⌈max_dias / 2⌉, 1)`.
    - **Valida: Requisitos 4.3, 4.4**

- [x] 9. Punto de control final — todos los tests deben pasar
  - Ejecutar `mvn test` y verificar que todas las pruebas (unitarias + property-based) pasan sin errores. Consultar al usuario si hay dudas antes de cerrar.

---

## Notes

- Las tareas marcadas con `*` son opcionales y pueden omitirse para una entrega MVP más rápida.
- El orden de las épicas respeta la regla de dependencia hexagonal: `domain` → `application` → `infrastructure`.
- Todos los servicios reciben sus dependencias exclusivamente por constructor; el wiring ocurre únicamente en `CotizadorConfiguration`.
- `Cotizacion` solo puede instanciarse mediante `Cotizacion.crear(...)`; nunca usar el constructor directamente.
- Los cálculos monetarios usan `BigDecimal` con escala 2 y `RoundingMode.HALF_UP`.
- Cada propiedad jqwik ejecuta un mínimo de 100 iteraciones por defecto.
- Los DTOs y el mapper viven exclusivamente en `infrastructure.adapter.in.rest`.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["2.3"] },
    { "id": 3, "tasks": ["2.4", "2.5"] },
    { "id": 4, "tasks": ["2.6", "2.7", "2.8", "2.9"] },
    { "id": 5, "tasks": ["2.10"] },
    { "id": 6, "tasks": ["2.11", "2.12", "2.13", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6"] },
    { "id": 7, "tasks": ["3.7", "3.8"] },
    { "id": 8, "tasks": ["3.9"] },
    { "id": 9, "tasks": ["3.10", "3.11", "3.12", "5.1", "5.2", "5.3"] },
    { "id": 10, "tasks": ["5.4", "5.5", "5.6", "6.1", "6.2", "6.3", "6.4"] },
    { "id": 11, "tasks": ["6.5"] },
    { "id": 12, "tasks": ["6.6", "6.7", "6.8", "6.9"] },
    { "id": 13, "tasks": ["6.10"] },
    { "id": 14, "tasks": ["6.11", "6.12", "6.13", "6.14", "6.15"] },
    { "id": 15, "tasks": ["8.1", "8.2", "8.3", "8.4", "8.5", "8.6", "8.7", "8.8", "8.9", "8.10"] }
  ]
}
```
