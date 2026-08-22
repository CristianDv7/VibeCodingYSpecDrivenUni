/**
 * app.js — Orquestador de la SPA Cotizador de Reparación de Calzados.
 *
 * Conecta el DOM con state.js y con el módulo de API (real o mock).
 * Registra los event listeners y gestiona las transiciones de estado de pantalla.
 */

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

// Producción — línea activa:
import * as api from './api.js'
// Desarrollo con mock — cambiar solo esta línea:
// import * as api from './mock.js'

import * as state from './state.js'

// ---------------------------------------------------------------------------
// Lógica de selección
// ---------------------------------------------------------------------------

/**
 * Indica si la selección actual es suficiente para habilitar el botón "Cotizar".
 * Retorna true si hay un tipo de calzado seleccionado (id no vacío) Y al menos
 * una reparación marcada.
 *
 * @returns {boolean}
 */
function seleccionEsValida() {
  const seleccion = state.getSeleccion()
  return seleccion.tipoCalzadoId !== '' && seleccion.reparacionIds.length >= 1
}

/**
 * Construye el cuerpo del POST a partir del estado de selección actual,
 * traduciendo los nombres internos del estado a los del contrato OpenAPI
 * (`calzadoId`, `esUrgente`).
 *
 * @returns {{ calzadoId: string, reparacionIds: string[], esUrgente: boolean }}
 */
function construirRequestCotizacion() {
  const { tipoCalzadoId, reparacionIds, urgente } = state.getSeleccion()
  return { calzadoId: tipoCalzadoId, reparacionIds, esUrgente: urgente }
}

// ---------------------------------------------------------------------------
// Gestión de estados de pantalla
// ---------------------------------------------------------------------------

/**
 * Estado_Cargando: inserta una opción "Cargando..." en el selector de calzado,
 * reemplaza el contenido de #lista-reparaciones con un párrafo indicador y
 * deshabilita el botón "Cotizar".
 */
function mostrarEstadoCargando() {
  const select = document.getElementById('tipo-calzado-select')
  select.innerHTML = '<option value="">Cargando...</option>'

  const lista = document.getElementById('lista-reparaciones')
  lista.innerHTML = '<p>Cargando...</p>'

  document.getElementById('boton-cotizar').disabled = true
}

/**
 * Estado_Cotizando: deshabilita el botón "Cotizar" y oculta cualquier mensaje
 * de error previo mientras se espera la respuesta del POST.
 */
function mostrarEstadoCotizando() {
  document.getElementById('boton-cotizar').disabled = true
  document.getElementById('mensaje-error').classList.add('oculto')
}

/**
 * Re-evalúa si el botón "Cotizar" debe habilitarse o deshabilitarse según la
 * validez de la selección actual. Se llama desde actualizarUI() y al finalizar
 * cada solicitud de cotización.
 */
function actualizarBoton() {
  document.getElementById('boton-cotizar').disabled = !seleccionEsValida()
}

// ---------------------------------------------------------------------------
// Funciones de renderizado
// ---------------------------------------------------------------------------

/**
 * Rellena el selector de calzado y la lista de checkboxes de reparaciones con
 * los datos del catálogo almacenados en el estado.
 *
 * #tipo-calzado-select: placeholder + una <option> por tipoCalzado
 *   con value === id y textContent === nombre.
 * #lista-reparaciones: un <label><input type="checkbox"> por tipoReparacion
 *   con value === id y texto de etiqueta === nombre.
 */
function renderCatalogo() {
  const tiposCalzado = state.getTiposCalzado()
  const tiposReparacion = state.getTiposReparacion()

  // Reconstruir el selector de calzado
  const select = document.getElementById('tipo-calzado-select')
  select.innerHTML = '<option value="">Selecciona un tipo de calzado</option>'
  tiposCalzado.forEach((calzado) => {
    const option = document.createElement('option')
    option.value = calzado.id
    option.textContent = calzado.nombre
    select.appendChild(option)
  })

  // Reconstruir la lista de checkboxes de reparaciones
  const lista = document.getElementById('lista-reparaciones')
  lista.innerHTML = ''
  tiposReparacion.forEach((reparacion) => {
    const label = document.createElement('label')
    const checkbox = document.createElement('input')
    checkbox.type = 'checkbox'
    checkbox.value = reparacion.id
    label.appendChild(checkbox)
    label.appendChild(document.createTextNode(' ' + reparacion.nombre))
    lista.appendChild(label)
  })
}

/**
 * Muestra el Panel_Resultado con los valores de la cotización recibida.
 * Formatea los importes monetarios con dos decimales precedidos de "$"
 * (p.ej. $14.40) y el tiempo como "${n} día(s)".
 * Hace visible #resultado-cotizacion y oculta #mensaje-error.
 *
 * @param {{ subtotal: number, recargoUrgencia: number, total: number, tiempoEstimadoDias: number }} cotizacion
 */
function renderResultado(cotizacion) {
  /**
   * Formatea un número como cadena de moneda con el símbolo $.
   * Usa siempre dos decimales.
   *
   * @param {number} valor
   * @returns {string}
   */
  function formatearMoneda(valor) {
    return '$' + valor.toFixed(2)
  }

  document.getElementById('resultado-subtotal').textContent = formatearMoneda(cotizacion.subtotal)
  document.getElementById('resultado-recargo').textContent = formatearMoneda(cotizacion.recargoUrgencia)
  document.getElementById('resultado-total').textContent = formatearMoneda(cotizacion.total)
  document.getElementById('resultado-tiempo').textContent =
    cotizacion.tiempoEstimadoDias + ' día(s)'

  document.getElementById('resultado-cotizacion').classList.remove('oculto')
  document.getElementById('mensaje-error').classList.add('oculto')
}

/**
 * Muestra el Mensaje_Error con el texto recibido.
 * Oculta el Panel_Resultado si estaba visible.
 *
 * @param {string} mensaje
 */
function renderError(mensaje) {
  const errorEl = document.getElementById('mensaje-error')
  errorEl.textContent = mensaje
  errorEl.classList.remove('oculto')

  document.getElementById('resultado-cotizacion').classList.add('oculto')
}

/**
 * Oculta el Panel_Resultado y borra el contenido de sus campos de detalle
 * (#resultado-subtotal, #resultado-recargo, #resultado-total, #resultado-tiempo).
 */
function limpiarResultado() {
  document.getElementById('resultado-cotizacion').classList.add('oculto')

  document.getElementById('resultado-subtotal').textContent = ''
  document.getElementById('resultado-recargo').textContent = ''
  document.getElementById('resultado-total').textContent = ''
  document.getElementById('resultado-tiempo').textContent = ''
}

/**
 * Callback central registrado en state.onCambio(). Re-evalúa la UI completa:
 * - Si ultimaCotizacion pasó a null y el panel de resultado aún es visible,
 *   lo limpia y oculta.
 * - Siempre actualiza el estado del botón.
 */
function actualizarUI() {
  const ultimaCotizacion = state.getUltimaCotizacion()
  const panelResultado = document.getElementById('resultado-cotizacion')

  if (ultimaCotizacion === null && !panelResultado.classList.contains('oculto')) {
    limpiarResultado()
  }

  actualizarBoton()
}

// --- (Continúa en siguientes subtareas) ---

// ---------------------------------------------------------------------------
// Event listeners
// ---------------------------------------------------------------------------

/**
 * Registra todos los listeners del DOM:
 * - Cambio en el selector de calzado → actualiza state y limpiarResultado
 * - Cambio en cualquier checkbox de reparación → actualiza state y limpiarResultado
 * - Cambio en el checkbox de urgencia → actualiza state
 * - Click en botón "Cotizar" → dispara la solicitud de cotización
 */
function registrarEventListeners() {
  document.getElementById('tipo-calzado-select').addEventListener('change', (e) => {
    state.setTipoCalzado(e.target.value)
    state.setUltimaCotizacion(null)
  })

  document.getElementById('lista-reparaciones').addEventListener('change', (e) => {
    if (e.target.type === 'checkbox') {
      const checkboxes = document.querySelectorAll('#lista-reparaciones input[type="checkbox"]:checked')
      const ids = Array.from(checkboxes).map((cb) => cb.value)
      state.setReparaciones(ids)
      state.setUltimaCotizacion(null)
    }
  })

  document.getElementById('urgente-checkbox').addEventListener('change', (e) => {
    state.setUrgente(e.target.checked)
  })

  document.getElementById('boton-cotizar').addEventListener('click', async () => {
    mostrarEstadoCotizando()
    try {
      const request = construirRequestCotizacion()
      const cotizacion = await api.generarCotizacion(request)
      state.setUltimaCotizacion(cotizacion)
      renderResultado(cotizacion)
    } catch (error) {
      renderError('No se pudo generar la cotización: ' + error.message)
    } finally {
      actualizarBoton()
    }
  })
}

// ---------------------------------------------------------------------------
// Inicialización principal
// ---------------------------------------------------------------------------

/**
 * Punto de entrada de la aplicación.
 * Inicializa el estado, registra el observer y los listeners,
 * muestra el estado de carga y solicita el catálogo al backend.
 */
async function init() {
  state.inicializar()
  state.onCambio(actualizarUI)
  registrarEventListeners()
  mostrarEstadoCargando()

  try {
    const [tiposCalzado, tiposReparacion] = await Promise.all([
      api.obtenerTiposCalzado(),
      api.obtenerTiposReparacion(),
    ])
    state.setCatalogo(tiposCalzado, tiposReparacion)
    renderCatalogo()
    actualizarBoton()
  } catch (error) {
    renderError('No se pudo cargar el catálogo. Intenta recargar la página.')
    // Restaurar el select a un estado legible
    const select = document.getElementById('tipo-calzado-select')
    select.innerHTML = '<option value="">Error al cargar</option>'
  }
}

document.addEventListener('DOMContentLoaded', init)
