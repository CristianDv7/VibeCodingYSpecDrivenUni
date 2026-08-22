# Plan de Implementación: Cotizador Frontend

## Visión General

Implementación de la SPA Cotizador de Reparación de Calzados usando HTML, CSS y JavaScript nativo con ES Modules. La implementación sigue el orden de dependencias definido en el diseño técnico: estructura del proyecto → módulos de datos → módulo de API → orquestador → tests.

---

## Tareas

- [x] 1. Scaffolding del proyecto y configuración de Vitest
  - Crear la estructura de directorios `js/` y `css/` dentro del directorio raíz del proyecto.
  - Crear `package.json` con `"type": "module"`, dependencias de desarrollo `vitest` y `@vitest/coverage-v8`, dependencia `fast-check`, y scripts `"test": "vitest --run"` y `"test:watch": "vitest"`.
  - Crear `vitest.config.js` configurado con entorno `jsdom` y soporte para ES Modules nativos.
  - Archivos: `package.json`, `vitest.config.js`
  - _Requisitos: R8 (infraestructura de testing)_

- [x] 2. Crear `index.html`
  - Implementar el HTML completo con todos los IDs del DOM especificados en el diseño: `#tipo-calzado-select`, `#lista-reparaciones`, `#urgente-checkbox`, `#boton-cotizar`, `#resultado-cotizacion`, `#resultado-subtotal`, `#resultado-recargo`, `#resultado-total`, `#resultado-tiempo`, `#mensaje-error`.
  - El `#boton-cotizar` debe tener atributo `disabled` por defecto.
  - El `#resultado-cotizacion` y `#mensaje-error` deben tener clase `oculto` por defecto.
  - El `<script type="module" src="js/app.js">` debe estar al final del `<body>`.
  - Incluir `<meta name="viewport">`, `lang="es"`, `aria-live="polite"` en `#resultado-cotizacion` y `role="alert"` en `#mensaje-error`.
  - Archivos: `index.html`
  - _Requisitos: R1.2, R1.3, R1.4, R2.1, R3.3, R4.1, R5.1, R7.1_

- [x] 3. Crear `css/estilos.css`
  - Implementar la clase `.oculto` con `display: none`.
  - Implementar `#boton-cotizar:disabled` con `opacity: 0.5` y `cursor: not-allowed`.
  - Implementar los estilos del panel `#resultado-cotizacion`: borde, border-radius, padding, background, y el grid de dos columnas para `dl`.
  - Implementar los estilos de `#mensaje-error`: color de texto rojo, fondo y borde de error.
  - Archivos: `css/estilos.css`
  - _Requisitos: R4.2, R5.2, R6.1_

- [x] 4. Implementar `js/state.js`
  - Definir el objeto de estado interno con la shape exacta del diseño: `tiposCalzado`, `tiposReparacion`, `seleccion` (`tipoCalzadoId`, `reparacionIds`, `urgente`) y `ultimaCotizacion`.
  - Implementar y exportar todas las funciones públicas: `inicializar()`, `getTiposCalzado()`, `getTiposReparacion()`, `getSeleccion()` (retorna copia superficial), `getUltimaCotizacion()`, `setCatalogo()`, `setTipoCalzado()`, `setReparaciones()`, `setUrgente()`, `setUltimaCotizacion()`.
  - Implementar `onCambio(callback)`: registra un único callback que se invoca en cada setter.
  - `getSeleccion()` debe retornar una copia superficial (no referencia directa) para prevenir mutaciones externas.
  - Archivos: `js/state.js`
  - _Requisitos: R2.2, R2.3, R2.4, R3.6, R4.3, R7.1_

- [x] 5. Implementar `js/api.js`
  - Definir la constante `API_BASE_URL = '/api'`.
  - Implementar y exportar `obtenerTiposCalzado()`: `GET /api/tipos-calzado`, retorna `Promise<Array<TipoCalzado>>`.
  - Implementar y exportar `obtenerTiposReparacion()`: `GET /api/tipos-reparacion`, retorna `Promise<Array<TipoReparacion>>`.
  - Implementar y exportar `generarCotizacion(request)`: `POST /api/cotizaciones` con `Content-Type: application/json`. Si `!respuesta.ok`, leer el campo `error` del body JSON (con `.catch(() => ({}))` como fallback) y lanzar `new Error(cuerpo.error ?? respuesta.statusText)`.
  - Archivos: `js/api.js`
  - _Requisitos: R1.1, R1.5, R3.1, R3.5_

- [x] 6. Implementar `js/mock.js`
  - Definir `MOCK_DELAY_MS = 400` y la función auxiliar `esperar()` que retorna una promesa que se resuelve tras ese retardo.
  - Definir los datos fijos `TIPOS_CALZADO` y `TIPOS_REPARACION` exactamente con los valores del contrato OpenAPI especificados en R8.2.
  - Implementar la función interna `calcularCotizacion(tipoCalzado, reparaciones, urgente)` con las reglas RN-01 (subtotal = Σ precioBase × factorComplejidad), RN-02 (recargo = subtotal × 0.30 si urgente) y RN-03 (tiempoEstimadoDias = max(...); si urgente → ceil(tiempo / 2), mín 1).
  - Exportar `obtenerTiposCalzado()`, `obtenerTiposReparacion()` y `generarCotizacion(request)` con las mismas firmas que `api.js`.
  - `generarCotizacion` debe lanzar `Error('Debe seleccionar al menos una reparación')` si `request.reparacionIds` está vacío o ausente (simulando el 400).
  - La respuesta de `generarCotizacion` debe incluir `id: crypto.randomUUID()`, `fechaCreacion: new Date().toISOString()`, `tipoCalzado`, `reparaciones`, `urgente`, `subtotal`, `recargo`, `total`, `tiempoEstimadoDias`.
  - Archivos: `js/mock.js`
  - _Requisitos: R8.1, R8.2, R8.3, R8.4, R8.5_

- [ ] 7. Implementar `js/app.js`
  - [x] 7.1 Implementar funciones de utilidad y lógica de selección
    - Implementar `seleccionEsValida()`: retorna `true` si `tipoCalzadoId` es no vacío y `reparacionIds.length >= 1`.
    - Implementar `construirRequestCotizacion()`: retorna `{ tipoCalzadoId, reparacionIds, urgente }` desde el estado actual.
    - _Requisitos: R2.2, R2.3, R2.4, R3.1_

  - [x] 7.2 Implementar funciones de gestión de estados de pantalla
    - Implementar `mostrarEstadoCargando()`: agrega la opción "Cargando..." al selector, inserta `<p>Cargando...</p>` en `#lista-reparaciones`, deshabilita `#boton-cotizar`.
    - Implementar `mostrarEstadoCotizando()`: deshabilita `#boton-cotizar`, oculta `#mensaje-error`.
    - Implementar `actualizarBoton()`: habilita o deshabilita `#boton-cotizar` según `seleccionEsValida()`.
    - _Requisitos: R6.1, R6.2, R6.3_

  - [x] 7.3 Implementar funciones de renderizado
    - Implementar `renderCatalogo()`: rellena `#tipo-calzado-select` con `<option value="{id}">{nombre}</option>` por cada tipo de calzado; rellena `#lista-reparaciones` con `<label><input type="checkbox" value="{id}"> {nombre}</label>` por cada tipo de reparación.
    - Implementar `renderResultado(cotizacion)`: rellena `#resultado-subtotal`, `#resultado-recargo`, `#resultado-total`, `#resultado-tiempo` con los valores exactos; hace visible `#resultado-cotizacion` (quita `.oculto`); oculta `#mensaje-error` (agrega `.oculto`).
    - Implementar `renderError(mensaje)`: pone el texto en `#mensaje-error` y lo hace visible; oculta `#resultado-cotizacion`.
    - Implementar `limpiarResultado()`: oculta `#resultado-cotizacion` y vacía sus `<dd>`.
    - _Requisitos: R1.3, R1.4, R3.3, R3.4, R3.5, R4.2, R5.2, R5.5_

  - [~] 7.4 Implementar event listeners y handlers
    - Implementar `onCalzadoCambiado(e)`: llama a `state.setTipoCalzado(e.target.value)` y `state.setUltimaCotizacion(null)`.
    - Implementar `onReparacionesCambiadas(e)`: recolecta `value` de todos los checkboxes marcados en `#lista-reparaciones`, llama a `state.setReparaciones(ids)` y `state.setUltimaCotizacion(null)`.
    - Implementar `onUrgenteCambiado(e)`: llama a `state.setUrgente(e.target.checked)` y `state.setUltimaCotizacion(null)`.
    - Implementar `onCotizarClick()`: llama a `solicitarCotizacion()`.
    - Registrar todos los event listeners según la tabla del diseño.
    - _Requisitos: R2.2, R3.1, R4.3, R4.4, R7.2_

  - [~] 7.5 Implementar `actualizarUI`, `cargarCatalogo`, `solicitarCotizacion` e `inicializar`
    - Implementar `actualizarUI()`: si `ultimaCotizacion` es null y `#resultado-cotizacion` es visible → `limpiarResultado()`; siempre llama `actualizarBoton()`.
    - Implementar `cargarCatalogo()`: `mostrarEstadoCargando()`, `Promise.all([api.obtenerTiposCalzado(), api.obtenerTiposReparacion()])`, en éxito → `state.setCatalogo(...)` → `renderCatalogo()`, en error → `renderError(err.message)`.
    - Implementar `solicitarCotizacion()`: `mostrarEstadoCotizando()`, construir request, `api.generarCotizacion(request)`, en éxito → `state.setUltimaCotizacion(cotizacion)` → `renderResultado(cotizacion)`, en error → `renderError(err.message)`, en ambos → `actualizarBoton()`.
    - Implementar `inicializar()`: `state.inicializar()` → `state.onCambio(actualizarUI)` → registrar event listeners → ocultar panel y error → deshabilitar botón → `cargarCatalogo()`.
    - Importar `* as api from './api.js'` con comentario indicando cómo cambiar a `mock.js`.
    - Llamar `inicializar()` al final del módulo.
    - _Requisitos: R1.1, R1.2, R1.5, R1.6, R2.1, R3.2, R3.7, R4.1, R5.1, R5.3, R6.4, R6.5, R6.6_

- [~] 8. Checkpoint — Verificar integración básica con mock
  - Asegurarse de que todos los archivos existen. Cambiar el import en `app.js` a `mock.js` y abrir `index.html` en el navegador para verificar manualmente que la carga del catálogo funciona, el botón se habilita al seleccionar calzado y reparación, y se muestra el resultado al cotizar. Volver el import a `api.js` si se cambió.
  - Asegurarse de que `package.json` está correcto antes de continuar con los tests.

- [~] 9. Tests unitarios con Vitest
  - Crear `tests/unit/state.test.js`: verificar que `inicializar()` produce el estado por defecto (tiposCalzado y tiposReparacion vacíos, selección vacía, ultimaCotizacion null); verificar que `getSeleccion()` retorna una copia superficial (no referencia).
  - Crear `tests/unit/app.test.js` con `jsdom`: verificar estado inicial del DOM (botón deshabilitado, `#resultado-cotizacion` oculto, `#mensaje-error` oculto, checkbox desmarcado); verificar que tras un error simulado de `cargarCatalogo` el `#mensaje-error` se hace visible y el botón permanece deshabilitado; verificar que durante Estado_Cotizando el botón se deshabilita; verificar que `seleccionEsValida()` retorna `false` con selección vacía y `true` con selección completa.
  - Crear `tests/unit/mock.test.js`: verificar que `generarCotizacion` con `reparacionIds` vacío lanza `Error` con mensaje `'Debe seleccionar al menos una reparación'`; verificar que las tres funciones del mock tardan al menos `MOCK_DELAY_MS` ms (400 ms).
  - Archivos: `tests/unit/state.test.js`, `tests/unit/app.test.js`, `tests/unit/mock.test.js`
  - _Requisitos: R1.5, R1.6, R2.1, R2.3, R2.4, R4.1, R5.1, R6.1, R6.3, R8.3, R8.5_

- [~] 10. Tests de propiedades — Propiedad 1: Renderizado fiel del catálogo
  - Crear `tests/properties/prop1.test.js`.
  - Usar fast-check para generar arrays arbitrarios de `TipoCalzado` (con `id` y `nombre` aleatorios) y `TipoReparacion` (con `id`, `nombre` aleatorios).
  - Verificar que `renderCatalogo()` produce exactamente un `<option>` por tipo de calzado con `value === id` y `textContent === nombre`, y exactamente un `<input type="checkbox">` con su `<label>` por tipo de reparación con `value === id` y texto de etiqueta igual a `nombre`.
  - Incluir comentario: `// Feature: cotizador-frontend, Propiedad 1: Renderizado fiel del catálogo`
  - Archivos: `tests/properties/prop1.test.js`
  - _Requisitos: R1.3, R1.4_

- [~] 11. Tests de propiedades — Propiedad 2: Estado del botón refleja la validez de la selección
  - Crear `tests/properties/prop2.test.js`.
  - Usar fast-check para generar combinaciones arbitrarias de `{ tipoCalzadoId: string | '', reparacionIds: string[] }`.
  - Verificar que cuando `tipoCalzadoId` es vacío o `reparacionIds` tiene longitud cero, `seleccionEsValida()` retorna `false` y `#boton-cotizar` tiene `disabled === true`; cuando ambos son válidos, retorna `true` y `disabled === false`.
  - Incluir comentario: `// Feature: cotizador-frontend, Propiedad 2: Estado del botón refleja la validez de la selección`
  - Archivos: `tests/properties/prop2.test.js`
  - _Requisitos: R2.3, R2.4_

- [~] 12. Tests de propiedades — Propiedad 3: `construirRequestCotizacion` mapea fielmente el estado
  - Crear `tests/properties/prop3.test.js`.
  - Usar fast-check para generar estados de selección válidos arbitrarios `{ tipoCalzadoId: nonEmptyString, reparacionIds: nonEmptyArray<string>, urgente: boolean }`.
  - Verificar que `construirRequestCotizacion()` retorna un objeto donde `request.tipoCalzadoId === tipoCalzadoId`, `request.reparacionIds` contiene exactamente los mismos elementos sin duplicados ni ausencias, y `request.urgente === urgente`.
  - Incluir comentario: `// Feature: cotizador-frontend, Propiedad 3: construirRequestCotizacion mapea fielmente el estado al cuerpo del POST`
  - Archivos: `tests/properties/prop3.test.js`
  - _Requisitos: R3.1_

- [~] 13. Tests de propiedades — Propiedad 4: `renderResultado` muestra los valores exactos
  - Crear `tests/properties/prop4.test.js`.
  - Usar fast-check para generar objetos `CotizacionResponse` con valores numéricos arbitrarios (`subtotal`, `recargo`, `total`, `tiempoEstimadoDias`).
  - Verificar que tras llamar `renderResultado(cotizacion)`: `#resultado-cotizacion` es visible (no tiene clase `oculto`), los textos de `#resultado-subtotal`, `#resultado-recargo`, `#resultado-total`, `#resultado-tiempo` contienen los valores exactos, y `#mensaje-error` es invisible.
  - Incluir comentario: `// Feature: cotizador-frontend, Propiedad 4: renderResultado muestra los valores exactos de la cotización`
  - Archivos: `tests/properties/prop4.test.js`
  - _Requisitos: R3.3, R4.2_

- [~] 14. Tests de propiedades — Propiedad 5: Selección se preserva tras error de API
  - Crear `tests/properties/prop5.test.js`.
  - Usar fast-check para generar estados de selección arbitrarios `{ tipoCalzadoId, reparacionIds, urgente }`.
  - Simular un POST fallido (mockear `api.generarCotizacion` para que lance un `Error`).
  - Verificar que después del error el estado de selección (`tipoCalzadoId`, `reparacionIds`, `urgente`) es idéntico al previo.
  - Incluir comentario: `// Feature: cotizador-frontend, Propiedad 5: La selección activa se preserva tras un error de API`
  - Archivos: `tests/properties/prop5.test.js`
  - _Requisitos: R3.6, R5.4_

- [~] 15. Tests de propiedades — Propiedad 6: Cambio en selección oculta el panel de resultado
  - Crear `tests/properties/prop6.test.js`.
  - Usar fast-check para generar estados en que `#resultado-cotizacion` es visible y tipos de cambio arbitrarios (modificar calzado, reparaciones o urgencia).
  - Verificar que tras cualquier modificación de la selección el `#resultado-cotizacion` queda invisible (tiene clase `oculto`) y su contenido queda vacío.
  - Incluir comentario: `// Feature: cotizador-frontend, Propiedad 6: Cualquier cambio en la selección oculta el panel de resultado`
  - Archivos: `tests/properties/prop6.test.js`
  - _Requisitos: R4.3, R4.4, R7.2_

- [~] 16. Checkpoint final — Verificar que todos los tests pasan
  - Ejecutar `npm test` y confirmar que todos los tests unitarios y de propiedades pasan sin errores.
  - Asegurarse de que no haya archivos temporales o configuraciones de test que interfieran con el proyecto.

---

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido.
- Cada tarea referencia requisitos específicos para trazabilidad.
- Los checkpoints garantizan validación incremental antes de continuar.
- Los tests de propiedades usan fast-check con mínimo 100 iteraciones por propiedad.
- Los tests unitarios cubren casos extremos y comportamientos de borde.
- El proyecto usa ES Modules nativos (`type="module"`) — Vitest con `jsdom` maneja esto sin transpilación.
- Para activar el Modo_Mock: cambiar la línea de import en `app.js` de `'./api.js'` a `'./mock.js'` (solo esa línea, sin otros cambios).

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2", "3", "4", "5", "6"] },
    { "id": 2, "tasks": ["7.1", "7.2", "7.3"] },
    { "id": 3, "tasks": ["7.4"] },
    { "id": 4, "tasks": ["7.5"] },
    { "id": 5, "tasks": ["9", "10", "11", "12", "13", "14", "15"] }
  ]
}
```
