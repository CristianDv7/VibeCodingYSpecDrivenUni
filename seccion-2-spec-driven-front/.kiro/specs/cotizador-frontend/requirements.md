# Documento de Requisitos

## Introducción

El **Cotizador de Reparación de Calzados** es una Single Page Application (SPA) implementada en HTML, CSS y JavaScript nativo que permite al usuario seleccionar un tipo de calzado, elegir una o más reparaciones y opcionalmente marcar urgencia, para luego obtener una cotización calculada por el backend. La pantalla única gestiona la carga del catálogo desde la API, la habilitación condicional del botón de cotización, la visualización del resultado y la presentación de mensajes de error.

---

## Glosario

- **App**: la aplicación frontend SPA en su conjunto.
- **Catálogo**: conjunto de tipos de calzado y tipos de reparación devueltos por la API al iniciar.
- **Panel_Resultado**: el elemento `#resultado-cotizacion` que muestra los datos de la última cotización exitosa.
- **Mensaje_Error**: el elemento `#mensaje-error` que informa al usuario sobre errores de API.
- **Boton_Cotizar**: el elemento `#boton-cotizar` que dispara la solicitud de cotización.
- **Selector_Calzado**: el elemento `#tipo-calzado-select` que permite elegir un tipo de calzado.
- **Lista_Reparaciones**: el elemento `#lista-reparaciones` que contiene los checkboxes de tipos de reparación.
- **Checkbox_Urgencia**: el elemento `#urgente-checkbox` que indica si el servicio es urgente.
- **Selección_Activa**: el conjunto formado por el tipo de calzado elegido, las reparaciones marcadas y el estado del Checkbox_Urgencia en un momento dado.
- **API**: el backend REST ya existente que provee el catálogo y calcula las cotizaciones.
- **Cotización**: el resultado devuelto por la API tras un POST exitoso, que incluye subtotal, recargo, total y tiempo estimado de entrega.
- **Estado_Cargando**: estado de la pantalla mientras la App espera la respuesta de la API para cargar el catálogo.
- **Estado_Listo**: estado de la pantalla con el catálogo cargado y sin Panel_Resultado visible.
- **Estado_Cotizando**: estado de la pantalla mientras la App espera la respuesta del POST a la API.
- **Estado_Resultado**: estado de la pantalla con el Panel_Resultado visible.
- **Estado_Error**: estado de la pantalla con el Mensaje_Error visible y el Panel_Resultado oculto.
- **Mock**: módulo `js/mock.js` que reemplaza a `api.js` durante el desarrollo, devolviendo datos fijos con la misma firma de funciones y estructura de respuesta que la API real.
- **Modo_Mock**: configuración de la App en la que `app.js` importa `mock.js` en lugar de `api.js`.

---

## Requisitos

### Requisito 1: Carga inicial del catálogo

**User Story:** Como usuario, quiero que al abrir la aplicación se carguen automáticamente los tipos de calzado y reparaciones disponibles, para poder realizar una cotización sin configuración adicional.

#### Criterios de Aceptación

1. WHEN la App se inicializa, THE App SHALL solicitar la lista de tipos de calzado al endpoint `GET /api/tipos-calzado` y la lista de tipos de reparación al endpoint `GET /api/tipos-reparacion`.
2. WHILE la App espera la respuesta de la API durante la carga inicial, THE Selector_Calzado SHALL mostrar un indicador de carga (texto "Cargando..." u opción deshabilitada equivalente) y THE Lista_Reparaciones SHALL mostrar un indicador de carga.
3. WHEN la API responde exitosamente a la carga inicial, THE Selector_Calzado SHALL mostrar una opción por cada tipo de calzado recibido, usando el campo `nombre` como texto visible.
4. WHEN la API responde exitosamente a la carga inicial, THE Lista_Reparaciones SHALL mostrar un checkbox por cada tipo de reparación recibida, usando el campo `nombre` como etiqueta visible.
5. IF la API no responde o devuelve un error durante la carga inicial, THEN THE App SHALL mostrar el Mensaje_Error con un texto descriptivo del fallo.
6. IF la API no responde o devuelve un error durante la carga inicial, THEN THE Boton_Cotizar SHALL permanecer deshabilitado y la pantalla no permitirá realizar cotizaciones.

---

### Requisito 2: Habilitación condicional del botón "Cotizar"

**User Story:** Como usuario, quiero que el botón "Cotizar" solo esté disponible cuando haya seleccionado un tipo de calzado y al menos una reparación, para evitar enviar una solicitud inválida.

#### Criterios de Aceptación

1. THE Boton_Cotizar SHALL estar deshabilitado por defecto al inicializar la App.
2. WHEN el usuario modifica la Selección_Activa, THE App SHALL evaluar si hay un tipo de calzado seleccionado en el Selector_Calzado Y al menos un checkbox marcado en la Lista_Reparaciones.
3. WHILE el Selector_Calzado no tiene ningún tipo de calzado seleccionado o la Lista_Reparaciones no tiene ningún checkbox marcado, THE Boton_Cotizar SHALL permanecer deshabilitado.
4. WHEN el Selector_Calzado tiene un tipo de calzado seleccionado Y al menos un checkbox de la Lista_Reparaciones está marcado, THE Boton_Cotizar SHALL estar habilitado.
5. WHEN el usuario desmarca todos los checkboxes de la Lista_Reparaciones, THE Boton_Cotizar SHALL deshabilitarse inmediatamente.
6. WHEN el usuario restablece el Selector_Calzado a su opción vacía o por defecto, THE Boton_Cotizar SHALL deshabilitarse inmediatamente.

---

### Requisito 3: Envío de la solicitud de cotización

**User Story:** Como usuario, quiero pulsar "Cotizar" y recibir el resultado calculado por el sistema, para conocer el costo y tiempo estimado de la reparación.

#### Criterios de Aceptación

1. WHEN el usuario pulsa el Boton_Cotizar, THE App SHALL enviar una solicitud `POST /api/cotizaciones` con el `tipoCalzadoId` del tipo de calzado seleccionado, el array `reparacionIds` de los checkboxes marcados y el campo `urgente` con el valor booleano del Checkbox_Urgencia.
2. WHILE la App espera la respuesta al POST, THE Boton_Cotizar SHALL estar deshabilitado para evitar envíos duplicados.
3. WHEN la API responde con código 201 al POST, THE App SHALL mostrar el Panel_Resultado con el subtotal, recargo, total y tiempo estimado de entrega en días recibidos en la respuesta.
4. WHEN la API responde con código 201 al POST, THE Mensaje_Error SHALL ocultarse si estaba visible.
5. IF la API responde con código 4xx o 5xx al POST, THEN THE App SHALL mostrar el Mensaje_Error con un texto descriptivo del error devuelto por la API.
6. IF la API responde con código 4xx o 5xx al POST, THEN THE Selección_Activa SHALL preservarse sin modificaciones.
7. WHEN la App recibe la respuesta al POST (exitosa o errónea), THE Boton_Cotizar SHALL recuperar su estado habilitado o deshabilitado según las condiciones del Requisito 2.

---

### Requisito 4: Visualización del panel de resultado

**User Story:** Como usuario, quiero ver claramente el desglose de la cotización (subtotal, recargo, total y tiempo de entrega), para entender el precio y plazo antes de aceptar.

#### Criterios de Aceptación

1. THE Panel_Resultado SHALL estar oculto por defecto al inicializar la App.
2. WHEN la App recibe una respuesta exitosa (201) al POST, THE Panel_Resultado SHALL volverse visible y mostrar los campos: subtotal, recargo, total y tiempo estimado de entrega en días, con los valores numéricos exactos devueltos por la API.
3. WHILE el Panel_Resultado está visible, THE App SHALL mantenerlo visible hasta que el usuario modifique la Selección_Activa o se cierre la sesión del navegador.
4. WHEN el usuario modifica cualquier elemento de la Selección_Activa mientras el Panel_Resultado está visible, THE Panel_Resultado SHALL ocultarse y su contenido SHALL borrarse.

---

### Requisito 5: Visualización del mensaje de error

**User Story:** Como usuario, quiero que los errores de la API se muestren de forma clara sin perder mis selecciones actuales, para poder corregir el problema y volver a intentarlo.

#### Criterios de Aceptación

1. THE Mensaje_Error SHALL estar oculto por defecto al inicializar la App.
2. IF la API devuelve un código 4xx o 5xx en cualquier solicitud, THEN THE Mensaje_Error SHALL mostrarse con un texto que describa el error.
3. WHEN el Mensaje_Error está visible y el usuario pulsa el Boton_Cotizar, THE Mensaje_Error SHALL ocultarse antes de mostrar el resultado de la nueva solicitud.
4. WHILE el Mensaje_Error está visible, THE Selección_Activa del usuario SHALL permanecer intacta.
5. WHEN el Mensaje_Error está visible y la nueva solicitud resulta exitosa, THE Mensaje_Error SHALL ocultarse y THE Panel_Resultado SHALL mostrarse.

---

### Requisito 6: Gestión de estados de pantalla

**User Story:** Como usuario, quiero recibir retroalimentación visual sobre lo que está ocurriendo en cada momento (carga, espera, resultado, error), para saber si la aplicación está respondiendo.

#### Criterios de Aceptación

1. WHILE la App se encuentra en Estado_Cargando, THE App SHALL mostrar indicadores de carga en el Selector_Calzado y en la Lista_Reparaciones, y THE Boton_Cotizar SHALL permanecer deshabilitado.
2. WHEN la carga inicial del catálogo finaliza exitosamente, THE App SHALL pasar al Estado_Listo, ocultando los indicadores de carga y mostrando el catálogo completo.
3. WHILE la App se encuentra en Estado_Cotizando (esperando respuesta al POST), THE Boton_Cotizar SHALL permanecer deshabilitado.
4. WHEN la respuesta al POST es exitosa, THE App SHALL pasar al Estado_Resultado, mostrando el Panel_Resultado y ocultando el Mensaje_Error.
5. IF la respuesta de la API (carga inicial o POST) es un error, THEN THE App SHALL pasar al Estado_Error, mostrando el Mensaje_Error y ocultando el Panel_Resultado.
6. WHEN el usuario modifica la Selección_Activa mientras la App está en Estado_Resultado, THE App SHALL pasar al Estado_Listo, ocultando el Panel_Resultado.

---

### Requisito 7: Checkbox de urgencia

**User Story:** Como usuario, quiero indicar si necesito el servicio de forma urgente, para que la cotización refleje el recargo correspondiente calculado por el backend.

#### Criterios de Aceptación

1. THE Checkbox_Urgencia SHALL estar desmarcado por defecto al inicializar la App.
2. WHEN el usuario marca o desmarca el Checkbox_Urgencia mientras el Panel_Resultado está visible, THE Panel_Resultado SHALL ocultarse y su contenido SHALL borrarse, siguiendo la regla UI-04.
3. WHEN el usuario pulsa el Boton_Cotizar, THE App SHALL incluir el valor booleano actual del Checkbox_Urgencia en el campo `urgente` del cuerpo del POST.

---

### Requisito 8: Modo mock para desarrollo sin backend

**User Story:** Como desarrollador, quiero poder ejecutar la App sin un backend real disponible, para poder desarrollar y verificar el comportamiento del frontend de forma independiente.

#### Criterios de Aceptación

1. THE App SHALL incluir un módulo `js/mock.js` que exporte funciones con la misma firma que `api.js` (`obtenerTiposCalzado()`, `obtenerTiposReparacion()`, `generarCotizacion(request)`), de modo que `api.js` pueda ser reemplazado por `mock.js` sin modificar ningún otro archivo.
2. WHEN el Modo_Mock está activo, THE Mock SHALL devolver datos fijos que reproduzcan exactamente el contrato OpenAPI: `obtenerTiposCalzado()` devuelve al menos `[{ id: "1", nombre: "Zapato formal", factorComplejidad: 1.2 }, { id: "2", nombre: "Bota de cuero", factorComplejidad: 1.5 }]`; `obtenerTiposReparacion()` devuelve al menos `[{ id: "1", nombre: "Cambio de tacón", precioBase: 12.00, tiempoEstimadoDias: 2 }, { id: "2", nombre: "Cambio de suela", precioBase: 20.00, tiempoEstimadoDias: 4 }]`; y `generarCotizacion(request)` calcula subtotal, recargo y total según las reglas de negocio RN-01 a RN-03 del backend, devolviendo una respuesta con la misma estructura que el endpoint `POST /api/cotizaciones` (código 201), incluyendo un `id` generado localmente y `fechaCreacion` con la fecha actual.
3. WHEN el Modo_Mock está activo y `generarCotizacion` recibe un `request` sin `reparacionIds` o con array vacío, THE Mock SHALL rechazar la solicitud devolviendo un error equivalente al código 400 con el mensaje `"Debe seleccionar al menos una reparación"`, para que el manejo de errores del Requisito 5 pueda verificarse también sin backend.
4. THE Modo_Mock SHALL activarse cambiando una única línea en `app.js` (el import que apunta a `api.js` o a `mock.js`), sin requerir ningún otro cambio en el código de la App.
5. THE Mock SHALL incluir un retardo artificial configurable (por defecto 400 ms) en todas sus funciones, para simular la latencia de red y permitir verificar los estados Estado_Cargando y Estado_Cotizando del Requisito 6.
