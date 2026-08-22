# Documento de Diseño Técnico — Cotizador Frontend

## Visión General

El Cotizador de Reparación de Calzados es una SPA implementada con HTML, CSS y JavaScript nativo (sin frameworks ni bundlers). Utiliza ES Modules nativos (`type="module"`) y se organiza en cuatro módulos JavaScript con responsabilidades bien delimitadas.

### Diagrama de arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                         index.html                          │
│   <script type="module" src="js/app.js">                    │
└──────────────────────────┬──────────────────────────────────┘
                           │ importa
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                          app.js                              │
│  • Inicialización                                            │
│  • Event listeners (DOM)                                     │
│  • Funciones de renderizado                                  │
│  • Gestión de los 5 estados de pantalla                      │
└─────────────┬──────────────────────────┬────────────────────┘
              │ importa                  │ importa
              ▼                          ▼
┌─────────────────────┐      ┌───────────────────────────┐
│      state.js       │      │  api.js  ─ó─  mock.js     │
│  • Estado en memoria│      │  • Adaptador HTTP          │
│  • onCambio()       │      │  • Misma firma pública     │
└─────────────────────┘      └───────────────────────────┘
                                         │ fetch / lógica local
                                         ▼
                              ┌─────────────────────┐
                              │  API REST backend    │
                              │  /api/tipos-calzado  │
                              │  /api/tipos-reparacion│
                              │  /api/cotizaciones    │
                              └─────────────────────┘
```

### Flujo principal de estados de pantalla

```
   Inicialización
        │
        ▼
[Estado_Cargando] ──error──► [Estado_Error]
        │
   carga OK
        ▼
 [Estado_Listo] ◄──── usuario modifica selección
        │
   pulsa Cotizar
        ▼
[Estado_Cotizando] ──error──► [Estado_Error]
        │
   201 OK
        ▼
[Estado_Resultado]
```

---

## Estructura de Archivos

```
cotizador-frontend/
├── index.html
├── css/
│   └── estilos.css
└── js/
    ├── state.js      ← estado en memoria + observer ligero
    ├── api.js        ← adaptador HTTP hacia la API real
    ├── mock.js       ← drop-in replacement de api.js para desarrollo
    └── app.js        ← orquestador: DOM + state + api/mock
```

---

## Diseño de `state.js`

### Estado interno

```js
// Shape del objeto de estado (privado al módulo)
{
  tiposCalzado: [],        // Array<{ id: string, nombre: string, factorComplejidad: number }>
  tiposReparacion: [],     // Array<{ id: string, nombre: string, precioBase: number, tiempoEstimadoDias: number }>
  seleccion: {
    tipoCalzadoId: '',     // string — vacío si no hay selección
    reparacionIds: [],     // Array<string>
    urgente: false         // boolean
  },
  ultimaCotizacion: null   // CotizacionResponse | null
}
```

### Exports públicos

```js
// Inicializa el estado a sus valores por defecto
export function inicializar(): void

// Getters
export function getTiposCalzado(): Array<TipoCalzado>
export function getTiposReparacion(): Array<TipoReparacion>
export function getSeleccion(): Seleccion   // devuelve copia (no referencia)
export function getUltimaCotizacion(): CotizacionResponse | null

// Setters — cada uno dispara el callback registrado con onCambio
export function setCatalogo(tiposCalzado: Array<TipoCalzado>, tiposReparacion: Array<TipoReparacion>): void
export function setTipoCalzado(id: string): void
export function setReparaciones(ids: Array<string>): void
export function setUrgente(valor: boolean): void
export function setUltimaCotizacion(cotizacion: CotizacionResponse | null): void

// Observer ligero — permite registrar un único callback que se invoca
// en cada modificación del estado. app.js lo usa para re-evaluar la UI.
export function onCambio(callback: () => void): void
```

**Notas:**
- `getSeleccion()` devuelve una copia superficial del objeto `seleccion` para evitar mutaciones externas accidentales.
- `onCambio` sobrescribe cualquier callback previo (no es un sistema de múltiples suscriptores — la app solo necesita uno).
- Ninguna función del módulo accede al DOM; state.js es puro lógica de datos.

---

## Diseño de `api.js`

### Constantes

```js
const API_BASE_URL = '/api'   // sin barra final
```

### Manejo de errores

Todas las funciones lanzarán un `Error` si:
- La petición falla a nivel de red (`fetch` rechaza la promesa), o
- La respuesta HTTP tiene un código `4xx` o `5xx`.

El mensaje del `Error` será el texto descriptivo del error: si la respuesta incluye un body JSON con campo `error`, se usará ese texto; si no, se usará el `statusText` HTTP.

### Exports públicos

```js
// Solicita la lista de tipos de calzado
// GET /api/tipos-calzado
// Retorna: Promise<Array<{ id: string, nombre: string, factorComplejidad: number }>>
// Lanza: Error con mensaje descriptivo si la petición falla
export async function obtenerTiposCalzado(): Promise<Array<TipoCalzado>>

// Solicita la lista de tipos de reparación
// GET /api/tipos-reparacion
// Retorna: Promise<Array<{ id: string, nombre: string, precioBase: number, tiempoEstimadoDias: number }>>
// Lanza: Error con mensaje descriptivo si la petición falla
export async function obtenerTiposReparacion(): Promise<Array<TipoReparacion>>

// Envía la solicitud de cotización
// POST /api/cotizaciones
// Parámetro request: { tipoCalzadoId: string, reparacionIds: string[], urgente: boolean }
// Retorna: Promise<CotizacionResponse> — el body JSON de la respuesta 201
// Lanza: Error con mensaje descriptivo si el código es 4xx o 5xx
export async function generarCotizacion(request: CotizacionRequest): Promise<CotizacionResponse>
```

### Implementación interna de `generarCotizacion`

```js
// Pseudocódigo
async function generarCotizacion(request) {
  const respuesta = await fetch(`${API_BASE_URL}/cotizaciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => ({}))
    throw new Error(cuerpo.error ?? respuesta.statusText)
  }
  return respuesta.json()
}
```

---

## Diseño de `mock.js`

### Propósito y contrato

`mock.js` es un **drop-in replacement** de `api.js`: exporta exactamente las mismas funciones con las mismas firmas. `app.js` importa uno u otro sin modificar ningún otro código.

### Constantes

```js
const MOCK_DELAY_MS = 400   // latencia artificial en milisegundos

// Datos fijos del catálogo (contrato OpenAPI)
const TIPOS_CALZADO = [
  { id: '1', nombre: 'Zapato formal',  factorComplejidad: 1.2 },
  { id: '2', nombre: 'Bota de cuero', factorComplejidad: 1.5 }
]

const TIPOS_REPARACION = [
  { id: '1', nombre: 'Cambio de tacón', precioBase: 12.00, tiempoEstimadoDias: 2 },
  { id: '2', nombre: 'Cambio de suela', precioBase: 20.00, tiempoEstimadoDias: 4 }
]
```

### Función auxiliar de retardo

```js
// Retorna una promesa que se resuelve tras MOCK_DELAY_MS ms
function esperar(): Promise<void>
```

### Implementación de las reglas de negocio (RN-01 a RN-03)

```js
// Cálculo interno — no exportado
function calcularCotizacion(tipoCalzado, reparaciones, urgente) {
  // RN-01: subtotal = Σ (precioBase × factorComplejidad)
  const subtotal = reparaciones.reduce(
    (acc, rep) => acc + rep.precioBase * tipoCalzado.factorComplejidad,
    0
  )

  // RN-02: recargo = subtotal × 0.30 si urgente, 0 si no
  const recargo = urgente ? subtotal * 0.30 : 0
  const total = subtotal + recargo

  // RN-03: tiempoEstimadoDias = max(tiempoEstimadoDias); si urgente → ceil(tiempo / 2), mín 1
  const tiempoBase = Math.max(...reparaciones.map(r => r.tiempoEstimadoDias))
  const tiempoEstimadoDias = urgente ? Math.max(1, Math.ceil(tiempoBase / 2)) : tiempoBase

  return { subtotal, recargo, total, tiempoEstimadoDias }
}
```

### Exports públicos

```js
// Retorna los tipos de calzado fijos tras MOCK_DELAY_MS ms
export async function obtenerTiposCalzado(): Promise<Array<TipoCalzado>>

// Retorna los tipos de reparación fijos tras MOCK_DELAY_MS ms
export async function obtenerTiposReparacion(): Promise<Array<TipoReparacion>>

// Calcula la cotización localmente con RN-01/02/03 tras MOCK_DELAY_MS ms
// Lanza un Error equivalente al código 400 si request.reparacionIds está vacío o ausente
export async function generarCotizacion(request: CotizacionRequest): Promise<CotizacionResponse>
```

### Simulación del error 400

```js
// Pseudocódigo dentro de generarCotizacion
await esperar()
if (!request.reparacionIds || request.reparacionIds.length === 0) {
  throw new Error('Debe seleccionar al menos una reparación')
}
// ... resto del cálculo
```

### Respuesta de `generarCotizacion` en Modo_Mock

```js
// Shape del objeto resuelto (misma estructura que el endpoint 201 real)
{
  id: crypto.randomUUID(),   // id local generado
  fechaCreacion: new Date().toISOString(),
  tipoCalzado: { /* objeto TipoCalzado seleccionado */ },
  reparaciones: [ /* array de objetos TipoReparacion seleccionados */ ],
  urgente: boolean,
  subtotal: number,
  recargo: number,
  total: number,
  tiempoEstimadoDias: number
}
```

---

## Diseño de `app.js`

### Responsabilidades

`app.js` es el orquestador de la aplicación: conecta el DOM con `state.js` y con el módulo de API (real o mock), registra los event listeners y gestiona las transiciones de estado de pantalla.

### Import intercambiable (R8)

```js
// Producción — línea activa:
import * as api from './api.js'

// Desarrollo con mock — cambiar solo esta línea:
// import * as api from './mock.js'
```

### Función de inicialización

```js
// Punto de entrada principal — se invoca al cargar el módulo
async function inicializar(): Promise<void>
```

**Secuencia:**
1. Llama a `state.inicializar()`
2. Registra `state.onCambio(actualizarUI)` para re-renderizar al cambiar el estado
3. Registra todos los event listeners del DOM
4. Aplica el estado inicial de la UI (ocultar panel resultado, ocultar error, deshabilitar botón)
5. Llama a `cargarCatalogo()`

### Carga del catálogo

```js
async function cargarCatalogo(): Promise<void>
```

**Secuencia:**
1. Llama a `mostrarEstadoCargando()`
2. Hace `Promise.all([api.obtenerTiposCalzado(), api.obtenerTiposReparacion()])` en paralelo
3. En éxito: llama a `state.setCatalogo(...)` y luego `renderCatalogo()`
4. En error: llama a `renderError(err.message)`

### Event listeners registrados

| Elemento DOM           | Evento   | Handler                     |
|------------------------|----------|-----------------------------|
| `#tipo-calzado-select` | `change` | `onCalzadoCambiado(e)`      |
| `#lista-reparaciones`  | `change` | `onReparacionesCambiadas(e)`|
| `#urgente-checkbox`    | `change` | `onUrgenteCambiado(e)`      |
| `#boton-cotizar`       | `click`  | `onCotizarClick()`          |

**`onCalzadoCambiado(e)`**: llama a `state.setTipoCalzado(e.target.value)` y `state.setUltimaCotizacion(null)`.

**`onReparacionesCambiadas(e)`**: recolecta todos los checkboxes marcados en `#lista-reparaciones`, extrae sus `value`, llama a `state.setReparaciones(ids)` y `state.setUltimaCotizacion(null)`.

**`onUrgenteCambiado(e)`**: llama a `state.setUrgente(e.target.checked)` y `state.setUltimaCotizacion(null)`.

**`onCotizarClick()`**: llama a `solicitarCotizacion()`.

### Construcción del request

```js
// Arma el body del POST a partir del estado actual
// Retorna: { tipoCalzadoId: string, reparacionIds: string[], urgente: boolean }
function construirRequestCotizacion(): CotizacionRequest
```

### Solicitud de cotización

```js
async function solicitarCotizacion(): Promise<void>
```

**Secuencia:**
1. Llama a `mostrarEstadoCotizando()` (deshabilita botón, oculta error)
2. Construye el request con `construirRequestCotizacion()`
3. Llama a `api.generarCotizacion(request)`
4. En éxito: llama a `state.setUltimaCotizacion(cotizacion)` y `renderResultado(cotizacion)`
5. En error: llama a `renderError(err.message)`
6. En ambos casos: llama a `actualizarBoton()` para restaurar el estado habilitado/deshabilitado

### Funciones de renderizado

```js
// Renderiza las opciones del selector y los checkboxes con los datos del catálogo
function renderCatalogo(): void

// Muestra el Panel_Resultado con los valores de la cotización
// Hace visible #resultado-cotizacion, oculta #mensaje-error
function renderResultado(cotizacion: CotizacionResponse): void

// Muestra el Mensaje_Error con el texto recibido
// Oculta #resultado-cotizacion, hace visible #mensaje-error
function renderError(mensaje: string): void

// Oculta el Panel_Resultado y borra su contenido
function limpiarResultado(): void
```

### Gestión de estados de pantalla

```js
// Estado_Cargando — selector y lista con texto "Cargando...", botón deshabilitado
function mostrarEstadoCargando(): void

// Estado_Cotizando — solo deshabilita el botón y oculta el error previo
function mostrarEstadoCotizando(): void

// Re-evalúa si el botón debe habilitarse o deshabilitarse
// Se llama desde state.onCambio y al finalizar la solicitud
function actualizarBoton(): void

// Función central de re-renderizado, registrada como callback en state.onCambio
// Evalúa el estado completo y actualiza la UI en consecuencia:
// - Si ultimaCotizacion es null y panel está visible → limpiarResultado()
// - Llama actualizarBoton()
function actualizarUI(): void
```

### Lógica de habilitación del botón

```js
// Retorna true si hay tipoCalzadoId no vacío Y reparacionIds.length > 0
function seleccionEsValida(): boolean
```

---

## Diseño de `index.html`

### Estructura de secciones

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Cotizador de Reparación de Calzados</title>
  <link rel="stylesheet" href="css/estilos.css">
</head>
<body>

  <main>
    <h1>Cotizador de Reparación de Calzados</h1>

    <!-- Sección: Selección de tipo de calzado -->
    <section id="seccion-calzado">
      <label for="tipo-calzado-select">Tipo de calzado</label>
      <select id="tipo-calzado-select">
        <option value="">Cargando...</option>
      </select>
    </section>

    <!-- Sección: Lista de reparaciones -->
    <section id="seccion-reparaciones">
      <fieldset>
        <legend>Reparaciones</legend>
        <!-- Los checkboxes se insertan dinámicamente -->
        <div id="lista-reparaciones">
          <p>Cargando...</p>
        </div>
      </fieldset>
    </section>

    <!-- Sección: Urgencia -->
    <section id="seccion-urgencia">
      <label>
        <input type="checkbox" id="urgente-checkbox">
        Servicio urgente
      </label>
    </section>

    <!-- Botón de acción -->
    <button type="button" id="boton-cotizar" disabled>Cotizar</button>

    <!-- Panel de resultado (oculto por defecto) -->
    <section id="resultado-cotizacion" class="oculto" aria-live="polite">
      <h2>Resultado de la cotización</h2>
      <dl>
        <dt>Subtotal</dt>
        <dd id="resultado-subtotal"></dd>
        <dt>Recargo por urgencia</dt>
        <dd id="resultado-recargo"></dd>
        <dt>Total</dt>
        <dd id="resultado-total"></dd>
        <dt>Tiempo estimado de entrega</dt>
        <dd id="resultado-tiempo"></dd>
      </dl>
    </section>

    <!-- Mensaje de error (oculto por defecto) -->
    <p id="mensaje-error" class="oculto" role="alert"></p>

  </main>

  <script type="module" src="js/app.js"></script>
</body>
</html>
```

**IDs del DOM:**

| ID                      | Elemento    | Propósito                                       |
|-------------------------|-------------|-------------------------------------------------|
| `#tipo-calzado-select`  | `<select>`  | Selector de tipo de calzado                     |
| `#lista-reparaciones`   | `<div>`     | Contenedor de checkboxes de reparaciones        |
| `#urgente-checkbox`     | `<input>`   | Checkbox de urgencia                            |
| `#boton-cotizar`        | `<button>`  | Dispara la solicitud de cotización              |
| `#resultado-cotizacion` | `<section>` | Panel de resultado (oculto/visible)             |
| `#resultado-subtotal`   | `<dd>`      | Valor numérico del subtotal                     |
| `#resultado-recargo`    | `<dd>`      | Valor numérico del recargo                      |
| `#resultado-total`      | `<dd>`      | Valor numérico del total                        |
| `#resultado-tiempo`     | `<dd>`      | Tiempo estimado de entrega en días              |
| `#mensaje-error`        | `<p>`       | Texto del error de API                          |

---

## Diseño de `estilos.css`

### Clase de visibilidad

```css
/* Utilidad principal para mostrar/ocultar elementos */
.oculto {
  display: none;
}
```

`app.js` usa `elemento.classList.add('oculto')` para ocultar y `elemento.classList.remove('oculto')` para mostrar. No se usan propiedades `visibility` ni `opacity` para garantizar que los elementos ocultos no ocupen espacio en el layout.

### Estado del botón deshabilitado

```css
#boton-cotizar:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

El atributo `disabled` lo gestiona `app.js` directamente: `boton.disabled = true/false`.

### Panel de resultado

```css
#resultado-cotizacion {
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 1rem;
  margin-top: 1rem;
  background-color: #f9f9f9;
}

#resultado-cotizacion dl {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.25rem 1rem;
}

#resultado-cotizacion dt {
  font-weight: bold;
}
```

### Mensaje de error

```css
#mensaje-error {
  color: #c0392b;
  background-color: #fdecea;
  border: 1px solid #e74c3c;
  border-radius: 4px;
  padding: 0.75rem 1rem;
  margin-top: 1rem;
}
```

---

## Flujo de Datos Completo

### Carga inicial del catálogo

```
Usuario abre index.html
  → app.js se carga como módulo ES
  → inicializar()
      → state.inicializar()                    // estado en blanco
      → state.onCambio(actualizarUI)           // registra observer
      → [event listeners registrados]
      → aplicarEstadoInicial()                 // oculta panel y error, deshabilita botón
      → cargarCatalogo()
          → mostrarEstadoCargando()            // "Cargando..." en selector y lista
          → Promise.all([
              api.obtenerTiposCalzado(),       // GET /api/tipos-calzado
              api.obtenerTiposReparacion()     // GET /api/tipos-reparacion
            ])
            ┌─ éxito ──────────────────────────────────────────────────────────┐
            │ → state.setCatalogo(calzados, reparaciones)                       │
            │ → state.onCambio dispara actualizarUI()                           │
            │ → renderCatalogo()                                                │
            │     → rellena #tipo-calzado-select con <option> por cada calzado │
            │     → rellena #lista-reparaciones con <input type="checkbox">     │
            │       por cada reparación                                         │
            └──────────────────────────────────────────────────────────────────┘
            ┌─ error ──────────────────────────────────────────────────────────┐
            │ → renderError(err.message)                                        │
            │     → hace visible #mensaje-error con el texto del error          │
            │     → mantiene #boton-cotizar deshabilitado                       │
            └──────────────────────────────────────────────────────────────────┘
```

### Solicitud de cotización

```
Usuario selecciona calzado y marca al menos una reparación
  → eventos change en #tipo-calzado-select y #lista-reparaciones
  → state.setTipoCalzado(id) / state.setReparaciones(ids)
  → state.onCambio dispara actualizarUI() → actualizarBoton()
  → #boton-cotizar se habilita

Usuario pulsa #boton-cotizar
  → onCotizarClick() → solicitarCotizacion()
      → mostrarEstadoCotizando()          // deshabilita botón, oculta error
      → request = construirRequestCotizacion()
          // { tipoCalzadoId, reparacionIds, urgente }
      → api.generarCotizacion(request)    // POST /api/cotizaciones
        ┌─ éxito 201 ───────────────────────────────────────────────────────────┐
        │ → state.setUltimaCotizacion(cotizacion)                                │
        │ → renderResultado(cotizacion)                                          │
        │     → rellena #resultado-subtotal, #resultado-recargo,                 │
        │       #resultado-total, #resultado-tiempo con los valores exactos      │
        │     → hace visible #resultado-cotizacion                               │
        │     → oculta #mensaje-error                                            │
        │ → actualizarBoton()  // re-habilita si selección sigue siendo válida   │
        └───────────────────────────────────────────────────────────────────────┘
        ┌─ error 4xx/5xx ───────────────────────────────────────────────────────┐
        │ → renderError(err.message)                                             │
        │     → hace visible #mensaje-error                                      │
        │     → oculta #resultado-cotizacion                                     │
        │ → selección en state.js permanece inalterada                           │
        │ → actualizarBoton()  // re-habilita si selección sigue siendo válida   │
        └───────────────────────────────────────────────────────────────────────┘
```

### Modificación de la selección con panel visible

```
Usuario modifica cualquier campo (calzado, reparaciones, urgencia)
  → handler correspondiente
  → state.setXxx(valor) + state.setUltimaCotizacion(null)
  → state.onCambio dispara actualizarUI()
      → ultimaCotizacion es null → limpiarResultado()
          → oculta #resultado-cotizacion y borra su contenido
      → actualizarBoton()
```

---

## Propiedades de Corrección

*Una propiedad es una característica o comportamiento que debe ser cierto en todas las ejecuciones válidas del sistema — esencialmente, un enunciado formal sobre lo que el sistema debe hacer. Las propiedades sirven como puente entre las especificaciones legibles por personas y las garantías de corrección verificables automáticamente.*

### Propiedad 1: Renderizado fiel del catálogo

*Para cualquier* array de tipos de calzado y tipos de reparación devuelto por la API, la función `renderCatalogo` debe producir exactamente una `<option>` por tipo de calzado (con `value === id` y `textContent === nombre`) y exactamente un `<input type="checkbox">` con su `<label>` por tipo de reparación (con `value === id` y texto de la etiqueta igual a `nombre`).

**Valida: Requisitos 1.3, 1.4**

---

### Propiedad 2: Estado del botón refleja la validez de la selección

*Para cualquier* estado de selección `{ tipoCalzadoId, reparacionIds, urgente }`:
- Si `tipoCalzadoId` es vacío (`''`) o `reparacionIds` tiene longitud cero, entonces `seleccionEsValida()` debe retornar `false` y `#boton-cotizar` debe tener `disabled === true`.
- Si `tipoCalzadoId` es no vacío y `reparacionIds.length >= 1`, entonces `seleccionEsValida()` debe retornar `true` y `#boton-cotizar` debe tener `disabled === false` (cuando no hay solicitud en curso).

**Valida: Requisitos 2.3, 2.4**

---

### Propiedad 3: `construirRequestCotizacion` mapea fielmente el estado al cuerpo del POST

*Para cualquier* estado de selección válido `{ tipoCalzadoId, reparacionIds, urgente }`, el objeto retornado por `construirRequestCotizacion()` debe satisfacer:
- `request.tipoCalzadoId === tipoCalzadoId`
- `request.reparacionIds` contiene exactamente los mismos elementos (sin duplicados ni ausencias) que `reparacionIds`
- `request.urgente === urgente`

**Valida: Requisito 3.1**

---

### Propiedad 4: `renderResultado` muestra los valores exactos de la cotización

*Para cualquier* objeto de cotización válido `{ subtotal, recargo, total, tiempoEstimadoDias }` recibido de la API, tras llamar a `renderResultado(cotizacion)`:
- `#resultado-cotizacion` es visible (no tiene la clase `oculto`)
- El texto de `#resultado-subtotal` contiene el valor numérico exacto de `subtotal`
- El texto de `#resultado-recargo` contiene el valor numérico exacto de `recargo`
- El texto de `#resultado-total` contiene el valor numérico exacto de `total`
- El texto de `#resultado-tiempo` contiene el valor numérico exacto de `tiempoEstimadoDias`
- `#mensaje-error` es invisible (tiene la clase `oculto`)

**Valida: Requisitos 3.3, 4.2**

---

### Propiedad 5: La selección activa se preserva tras un error de API

*Para cualquier* estado de selección `{ tipoCalzadoId, reparacionIds, urgente }` previo a una solicitud POST fallida (respuesta 4xx o 5xx), el estado de selección después del error debe ser idéntico al previo: `tipoCalzadoId`, `reparacionIds` y `urgente` sin modificaciones.

**Valida: Requisitos 3.6, 5.4**

---

### Propiedad 6: Cualquier cambio en la selección oculta el panel de resultado

*Para cualquier* estado en que `#resultado-cotizacion` es visible, cualquier modificación de `tipoCalzadoId`, `reparacionIds` o `urgente` debe resultar en que `#resultado-cotizacion` pase a ser invisible y su contenido quede vacío.

**Valida: Requisitos 4.3, 4.4, 7.2**

---

## Manejo de Errores

### Errores de red

`api.js` deja que la excepción nativa de `fetch` (tipo `TypeError`) se propague, siendo capturada por el `catch` de `cargarCatalogo()` o `solicitarCotizacion()`. El mensaje se muestra tal cual en `#mensaje-error`.

### Errores HTTP (4xx / 5xx)

`api.js` detecta `!respuesta.ok` e intenta leer el campo `error` del body JSON. Si el parsing falla o el campo no existe, usa `respuesta.statusText` como fallback.

### Estado del botón tras error

Tras cualquier error, se llama a `actualizarBoton()` para restaurar el estado correcto del botón (habilitado si la selección sigue siendo válida, deshabilitado si no).

### Invariante de selección

`renderError()` y `solicitarCotizacion()` no modifican nunca el estado de selección (`tipoCalzadoId`, `reparacionIds`, `urgente`). Solo modifican `ultimaCotizacion` (poniéndolo a `null` si no hay resultado nuevo).

---

## Estrategia de Testing

### Herramientas

Dado que el proyecto usa JavaScript nativo sin bundler, se recomienda **Vitest** con `jsdom` como entorno de DOM, que no requiere transpilación y soporta ES Modules nativos.

Para los tests de propiedades: **fast-check** (compatible con Vitest, sin dependencias de transpilación).

### Testing de unidad — ejemplos y casos extremos

- Inicialización: verificar que todos los elementos del DOM tienen el estado por defecto correcto (botón deshabilitado, panel y error ocultos, checkbox desmarcado)
- Carga del catálogo con error: verificar que `#mensaje-error` se hace visible y el botón permanece deshabilitado
- Transición Estado_Cotizando: verificar que el botón se deshabilita mientras se espera la respuesta
- Error 400 del mock: verificar que el mock lanza `Error` con el mensaje esperado cuando `reparacionIds` está vacío
- Retardo del mock: verificar que las tres funciones del mock tardan al menos `MOCK_DELAY_MS` ms

### Testing de propiedades (property-based)

Configuración de fast-check: mínimo 100 iteraciones por propiedad.
Cada test debe incluir un comentario con el tag:
**Feature: cotizador-frontend, Propiedad N: \<texto de la propiedad\>**

- **Propiedad 1** — Generadores: arrays arbitrarios de TipoCalzado y TipoReparacion con `id` y `nombre` aleatorios. Verificar counts y textos de los elementos renderizados.
- **Propiedad 2** — Generadores: combinaciones arbitrarias de `{ tipoCalzadoId: string | '', reparacionIds: string[] }`. Verificar el retorno de `seleccionEsValida()` y el atributo `disabled` del botón.
- **Propiedad 3** — Generadores: estados de selección válidos arbitrarios `{ tipoCalzadoId: nonEmptyString, reparacionIds: nonEmptyArray, urgente: boolean }`. Verificar la estructura del objeto retornado por `construirRequestCotizacion()`.
- **Propiedad 4** — Generadores: objetos `CotizacionResponse` con valores numéricos arbitrarios. Verificar visibilidad del panel y exactitud de los valores mostrados.
- **Propiedad 5** — Generadores: estados de selección arbitrarios. Simular error del POST y verificar que el estado de selección no cambia.
- **Propiedad 6** — Generadores: estados de selección y tipos de cambio arbitrarios (calzado, reparaciones, urgencia). Partir de panel visible, aplicar el cambio, verificar que el panel queda oculto.

---

## Decisiones de Diseño

### Por qué `mock.js` es un drop-in replacement y no un flag booleano

Un flag booleano (p.ej. `const USE_MOCK = true` en `app.js`) requeriría lógica condicional dentro de `api.js` o `app.js`, mezclando responsabilidades de producción y desarrollo en el mismo módulo. Con el patrón Adapter + drop-in replacement:

- `api.js` y `mock.js` son módulos independientes; ninguno conoce la existencia del otro.
- El contrato entre `app.js` y el módulo de API queda definido únicamente por las tres firmas exportadas.
- Cambiar de entorno requiere modificar exactamente una línea (el `import`), sin riesgo de afectar otra lógica.
- Los tests pueden importar `mock.js` directamente sin necesidad de configuración adicional.

### Por qué se usa un observer en `state.js`

El observer ligero (`onCambio`) centraliza la reacción a los cambios de estado: en lugar de que cada handler de evento llame directamente a las funciones de renderizado, todos los cambios pasan por `state.js` y se propagan a través de un único callback registrado en `app.js`. Esto garantiza que la UI siempre refleja el estado en memoria y evita la duplicación de llamadas a `actualizarBoton()` o `limpiarResultado()` en múltiples sitios del código. El coste es mínimo dado que solo hay un suscriptor.
