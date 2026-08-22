// Ruta relativa: el navegador envía las peticiones al mismo origen (Nginx en :80),
// que las proxia internamente al backend. Así funciona tanto en local como en producción
// sin necesidad de exponer el puerto 8080 del backend al host.
const API_BASE_URL = '/api'

/**
 * Solicita la lista de tipos de calzado.
 * GET /calzados
 * @returns {Promise<Array<{ id: string, nombre: string, factorComplejidad: number }>>}
 * @throws {Error} si la petición falla a nivel de red o la respuesta es 4xx/5xx
 */
export async function obtenerTiposCalzado() {
  const respuesta = await fetch(`${API_BASE_URL}/calzados`)
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => ({}))
    throw new Error(cuerpo.message ?? respuesta.statusText)
  }
  return respuesta.json()
}

/**
 * Solicita la lista de tipos de reparación.
 * GET /reparaciones
 * @returns {Promise<Array<{ id: string, nombre: string, precioBase: number, tiempoEstimadoDias: number }>>}
 * @throws {Error} si la petición falla a nivel de red o la respuesta es 4xx/5xx
 */
export async function obtenerTiposReparacion() {
  const respuesta = await fetch(`${API_BASE_URL}/reparaciones`)
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => ({}))
    throw new Error(cuerpo.message ?? respuesta.statusText)
  }
  return respuesta.json()
}

/**
 * Envía la solicitud de cotización.
 * POST /cotizaciones
 * @param {{ calzadoId: string, reparacionIds: string[], esUrgente: boolean }} request
 * @returns {Promise<Object>} el body JSON de la respuesta 201
 * @throws {Error} si la respuesta es 4xx/5xx o la petición falla a nivel de red
 */
export async function generarCotizacion(request) {
  const respuesta = await fetch(`${API_BASE_URL}/cotizaciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => ({}))
    throw new Error(cuerpo.message ?? respuesta.statusText)
  }
  return respuesta.json()
}
