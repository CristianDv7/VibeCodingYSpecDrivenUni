/**
 * state.js — Estado en memoria de la aplicación Cotizador de Reparación de Calzados.
 *
 * Módulo de datos puro: sin acceso al DOM. Expone getters, setters y un observer
 * ligero de un único suscriptor. Cada setter dispara el callback registrado con
 * onCambio(), si existe.
 */

// ---------------------------------------------------------------------------
// Estado privado
// ---------------------------------------------------------------------------

/**
 * @typedef {{ id: string, nombre: string, factorComplejidad: number }} TipoCalzado
 * @typedef {{ id: string, nombre: string, precioBase: number, tiempoEstimadoDias: number }} TipoReparacion
 * @typedef {{ tipoCalzadoId: string, reparacionIds: string[], urgente: boolean }} Seleccion
 * @typedef {object|null} CotizacionResponse
 */

/** @type {{ tiposCalzado: TipoCalzado[], tiposReparacion: TipoReparacion[], seleccion: Seleccion, ultimaCotizacion: CotizacionResponse }} */
let estado = _crearEstadoPorDefecto()

/** @type {(() => void) | null} */
let _callback = null

// ---------------------------------------------------------------------------
// Helpers internos
// ---------------------------------------------------------------------------

/** Devuelve un objeto de estado con todos los valores en sus valores por defecto. */
function _crearEstadoPorDefecto() {
  return {
    tiposCalzado: [],
    tiposReparacion: [],
    seleccion: {
      tipoCalzadoId: '',
      reparacionIds: [],
      urgente: false,
    },
    ultimaCotizacion: null,
  }
}

/** Invoca el callback registrado, si existe. */
function _notificar() {
  if (_callback) {
    _callback()
  }
}

// ---------------------------------------------------------------------------
// Inicialización
// ---------------------------------------------------------------------------

/**
 * Restablece el estado a sus valores por defecto.
 * No dispara el callback (es una operación de setup, no de cambio de datos).
 */
export function inicializar() {
  estado = _crearEstadoPorDefecto()
}

// ---------------------------------------------------------------------------
// Getters
// ---------------------------------------------------------------------------

/** @returns {TipoCalzado[]} */
export function getTiposCalzado() {
  return estado.tiposCalzado
}

/** @returns {TipoReparacion[]} */
export function getTiposReparacion() {
  return estado.tiposReparacion
}

/**
 * Devuelve una copia superficial de la selección activa para prevenir
 * mutaciones externas accidentales del objeto interno.
 *
 * @returns {Seleccion}
 */
export function getSeleccion() {
  return {
    tipoCalzadoId: estado.seleccion.tipoCalzadoId,
    reparacionIds: [...estado.seleccion.reparacionIds],
    urgente: estado.seleccion.urgente,
  }
}

/** @returns {CotizacionResponse} */
export function getUltimaCotizacion() {
  return estado.ultimaCotizacion
}

// ---------------------------------------------------------------------------
// Setters — cada uno dispara el callback registrado
// ---------------------------------------------------------------------------

/**
 * Actualiza el catálogo completo (tipos de calzado y tipos de reparación).
 *
 * @param {TipoCalzado[]} tiposCalzado
 * @param {TipoReparacion[]} tiposReparacion
 */
export function setCatalogo(tiposCalzado, tiposReparacion) {
  estado.tiposCalzado = tiposCalzado
  estado.tiposReparacion = tiposReparacion
  _notificar()
}

/**
 * Actualiza el tipo de calzado seleccionado.
 *
 * @param {string} id
 */
export function setTipoCalzado(id) {
  estado.seleccion.tipoCalzadoId = id
  _notificar()
}

/**
 * Actualiza el array de IDs de reparaciones seleccionadas.
 *
 * @param {string[]} ids
 */
export function setReparaciones(ids) {
  estado.seleccion.reparacionIds = ids
  _notificar()
}

/**
 * Actualiza el indicador de servicio urgente.
 *
 * @param {boolean} valor
 */
export function setUrgente(valor) {
  estado.seleccion.urgente = valor
  _notificar()
}

/**
 * Almacena la última cotización recibida de la API (o null para limpiarla).
 *
 * @param {CotizacionResponse} cotizacion
 */
export function setUltimaCotizacion(cotizacion) {
  estado.ultimaCotizacion = cotizacion
  _notificar()
}

// ---------------------------------------------------------------------------
// Observer ligero
// ---------------------------------------------------------------------------

/**
 * Registra el único callback que será invocado en cada modificación del estado.
 * Sobrescribe cualquier callback previamente registrado.
 *
 * @param {() => void} callback
 */
export function onCambio(callback) {
  _callback = callback
}
