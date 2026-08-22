/**
 * mock.js — Drop-in replacement de api.js para desarrollo sin backend.
 * Exporta las mismas funciones con las mismas firmas que api.js.
 * Para activar: cambiar el import en app.js de './api.js' a './mock.js'.
 */

// Latencia artificial configurable (milisegundos)
const MOCK_DELAY_MS = 400

// Datos fijos del catálogo (contrato OpenAPI — R8.2)
const TIPOS_CALZADO = [
  { id: '1', nombre: 'Zapato formal', factorComplejidad: 1.2 },
  { id: '2', nombre: 'Bota de cuero', factorComplejidad: 1.5 }
]

const TIPOS_REPARACION = [
  { id: '1', nombre: 'Cambio de tacón', precioBase: 12.00, tiempoEstimadoDias: 2 },
  { id: '2', nombre: 'Cambio de suela', precioBase: 20.00, tiempoEstimadoDias: 4 }
]

/**
 * Retorna una promesa que se resuelve tras MOCK_DELAY_MS ms.
 * @returns {Promise<void>}
 */
function esperar() {
  return new Promise((resolve) => setTimeout(resolve, MOCK_DELAY_MS))
}

/**
 * Calcula los valores de una cotización aplicando las reglas de negocio RN-01 a RN-03.
 *
 * RN-01: subtotal = Σ (rep.precioBase × tipoCalzado.factorComplejidad)
 * RN-02: recargo  = urgente ? subtotal × 0.30 : 0  ;  total = subtotal + recargo
 * RN-03: tiempoBase = max(...reparaciones.tiempoEstimadoDias)
 *         si urgente → ceil(tiempoBase / 2), mínimo 1
 *
 * @param {{ id: string, nombre: string, factorComplejidad: number }} tipoCalzado
 * @param {Array<{ id: string, nombre: string, precioBase: number, tiempoEstimadoDias: number }>} reparaciones
 * @param {boolean} urgente
 * @returns {{ subtotal: number, recargo: number, total: number, tiempoEstimadoDias: number }}
 */
function calcularCotizacion(tipoCalzado, reparaciones, urgente) {
  // RN-01
  const subtotal = reparaciones.reduce(
    (acc, rep) => acc + rep.precioBase * tipoCalzado.factorComplejidad,
    0
  )

  // RN-02
  const recargo = urgente ? subtotal * 0.30 : 0
  const total = subtotal + recargo

  // RN-03
  const tiempoBase = Math.max(...reparaciones.map((r) => r.tiempoEstimadoDias))
  const tiempoEstimadoDias = urgente ? Math.max(1, Math.ceil(tiempoBase / 2)) : tiempoBase

  return { subtotal, recargo, total, tiempoEstimadoDias }
}

/**
 * Retorna la lista de tipos de calzado tras una latencia artificial.
 * @returns {Promise<Array<{ id: string, nombre: string, factorComplejidad: number }>>}
 */
export async function obtenerTiposCalzado() {
  await esperar()
  return [...TIPOS_CALZADO]
}

/**
 * Retorna la lista de tipos de reparación tras una latencia artificial.
 * @returns {Promise<Array<{ id: string, nombre: string, precioBase: number, tiempoEstimadoDias: number }>>}
 */
export async function obtenerTiposReparacion() {
  await esperar()
  return [...TIPOS_REPARACION]
}

/**
 * Calcula una cotización localmente usando las reglas de negocio del backend.
 * Lanza un Error equivalente al código 400 si reparacionIds está vacío o ausente.
 *
 * @param {{ calzadoId: string, reparacionIds: string[], esUrgente: boolean }} request
 * @returns {Promise<{
 *   id: string,
 *   fechaCreacion: string,
 *   subtotal: number,
 *   recargoUrgencia: number,
 *   total: number,
 *   tiempoEstimadoDias: number
 * }>}
 */
export async function generarCotizacion(request) {
  await esperar()

  // Simula error 400 cuando no hay reparaciones seleccionadas (R8.3)
  if (!request.reparacionIds || request.reparacionIds.length === 0) {
    throw new Error('Debe seleccionar al menos una reparación')
  }

  const tipoCalzado = TIPOS_CALZADO.find((c) => c.id === request.calzadoId)
  const reparaciones = TIPOS_REPARACION.filter((r) => request.reparacionIds.includes(r.id))

  const { subtotal, recargo, total, tiempoEstimadoDias } = calcularCotizacion(
    tipoCalzado,
    reparaciones,
    request.esUrgente
  )

  return {
    id: crypto.randomUUID(),
    fechaCreacion: new Date().toISOString(),
    subtotal,
    recargoUrgencia: recargo,
    total,
    tiempoEstimadoDias
  }
}
