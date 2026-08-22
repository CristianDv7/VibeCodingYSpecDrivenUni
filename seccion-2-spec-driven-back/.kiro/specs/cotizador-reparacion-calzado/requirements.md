# Requirements Document

## Introduction

Este documento describe los requisitos funcionales del backend del **Cotizador de Reparación de Calzado**. El sistema permite a los clientes consultar los tipos de calzado y reparaciones disponibles, seleccionar una combinación y obtener una cotización estimada del costo total y el tiempo de entrega. Opcionalmente, el cliente puede indicar urgencia para recibir el cálculo del recargo correspondiente y el tiempo reducido de entrega.

El alcance está limitado a la generación de cotizaciones con persistencia en memoria. No incluye autenticación, pagos ni persistencia en base de datos.

---

## Glossary

- **Cotizador**: El sistema backend objeto de este documento.
- **Cotizacion**: Objeto de dominio que representa la estimación de costo y tiempo de entrega generada para un conjunto de reparaciones sobre un tipo de calzado. Contiene identificador único, fecha de creación, subtotal, recargo, total y tiempo estimado de entrega.
- **Calzado**: Tipo de calzado (e.g., zapatilla deportiva, bota de cuero) que posee un factor de complejidad utilizado en el cálculo del precio.
- **Factor_Complejidad**: Valor numérico decimal asociado a un tipo de calzado que pondera el precio base de cada reparación.
- **Reparacion**: Servicio de reparación disponible (e.g., cambio de suela, costura) que tiene un precio base y un tiempo estimado de entrega en días.
- **Precio_Base**: Costo base de una reparación antes de aplicar el factor de complejidad del calzado.
- **Subtotal**: Suma de (Precio_Base de cada Reparacion seleccionada × Factor_Complejidad del Calzado).
- **Recargo_Urgencia**: Monto adicional equivalente al 30 % del Subtotal, aplicado únicamente cuando el servicio es urgente.
- **Total**: Subtotal + Recargo_Urgencia (cuando aplica) o Subtotal (cuando no es urgente).
- **Tiempo_Estimado_Entrega**: Cantidad de días para completar el servicio. Se calcula como el máximo de los tiempos estimados de las reparaciones seleccionadas. Si el servicio es urgente, se reduce a la mitad redondeada hacia arriba, con un mínimo de 1 día.
- **Servicio_Urgente**: Indicador booleano de la cotización que determina si se aplica el Recargo_Urgencia y se reduce el Tiempo_Estimado_Entrega.
- **Identificador_Unico**: UUID generado en el momento de crear una Cotizacion que la identifica de forma irrepetible.

---

## Requirements

### Requisito 1: Consultar tipos de calzado disponibles

**Historia de usuario:** Como cliente, quiero consultar los tipos de calzado disponibles, para saber qué opciones puedo seleccionar al generar una cotización.

#### Criterios de aceptación

1. WHEN el cliente solicita la lista de tipos de calzado, THE Cotizador SHALL retornar la lista de tipos de calzado disponibles.
2. THE Cotizador SHALL incluir, para cada tipo de calzado retornado, su identificador único, nombre y Factor_Complejidad.

---

### Requisito 2: Consultar reparaciones disponibles

**Historia de usuario:** Como cliente, quiero consultar las reparaciones disponibles, para saber qué servicios puedo incluir en una cotización.

#### Criterios de aceptación

1. WHEN el cliente solicita la lista de reparaciones, THE Cotizador SHALL retornar todas las reparaciones registradas.
2. THE Cotizador SHALL incluir, para cada reparación retornada, su identificador único, nombre, Precio_Base y tiempo estimado de entrega en días.

---

### Requisito 3: Generar cotización sin urgencia (RN-01, RN-04, RN-05)

**Historia de usuario:** Como cliente, quiero seleccionar un tipo de calzado y una o más reparaciones para obtener una cotización estimada del costo total.

#### Criterios de aceptación

1. WHEN el cliente envía una solicitud de cotización con un Calzado válido y al menos una Reparacion válida, THE Cotizador SHALL calcular el Subtotal como la suma de (Precio_Base de cada Reparacion seleccionada × Factor_Complejidad del Calzado).
2. WHEN el cliente envía una solicitud de cotización sin indicar urgencia, THE Cotizador SHALL establecer el Total igual al Subtotal, con Recargo_Urgencia igual a cero.
3. WHEN el cliente envía una solicitud de cotización sin indicar urgencia, THE Cotizador SHALL calcular el Tiempo_Estimado_Entrega como el valor máximo entre los tiempos estimados de las reparaciones seleccionadas.
4. WHEN el cliente envía una solicitud de cotización válida, THE Cotizador SHALL asignar a la Cotizacion un Identificador_Unico y una fecha de creación.
5. WHEN el cliente envía una solicitud de cotización válida, THE Cotizador SHALL retornar la Cotizacion con su Identificador_Unico, fecha de creación, Subtotal, Recargo_Urgencia, Total y Tiempo_Estimado_Entrega.
6. IF el cliente envía una solicitud de cotización con una lista de reparaciones vacía o ausente, THEN THE Cotizador SHALL rechazar la solicitud con un mensaje de error que indique que se requiere al menos una reparación.
7. IF el cliente envía una solicitud de cotización con un identificador de Calzado que no existe en el repositorio, THEN THE Cotizador SHALL rechazar la solicitud con un mensaje de error que indique que el tipo de calzado no fue encontrado.
8. IF el cliente envía una solicitud de cotización con algún identificador de Reparacion que no existe en el repositorio, THEN THE Cotizador SHALL rechazar la solicitud con un mensaje de error que indique qué identificador de reparación no fue encontrado.

---

### Requisito 4: Generar cotización con urgencia (RN-02, RN-03)

**Historia de usuario:** Como cliente, quiero marcar el servicio como urgente para conocer el recargo aplicable y el nuevo tiempo estimado de entrega.

#### Criterios de aceptación

1. WHEN el cliente envía una solicitud de cotización con Servicio_Urgente igual a verdadero, THE Cotizador SHALL calcular el Recargo_Urgencia como el 30 % del Subtotal.
2. WHEN el cliente envía una solicitud de cotización con Servicio_Urgente igual a verdadero, THE Cotizador SHALL calcular el Total como la suma del Subtotal más el Recargo_Urgencia.
3. WHEN el cliente envía una solicitud de cotización con Servicio_Urgente igual a verdadero, THE Cotizador SHALL calcular el Tiempo_Estimado_Entrega como el valor máximo entre los tiempos estimados de las reparaciones seleccionadas, dividido entre dos y redondeado hacia arriba.
4. WHEN el cliente envía una solicitud de cotización con Servicio_Urgente igual a verdadero y el Tiempo_Estimado_Entrega calculado resulta menor a 1 día, THE Cotizador SHALL establecer el Tiempo_Estimado_Entrega en 1 día.
5. WHEN el cliente envía una solicitud de cotización con Servicio_Urgente igual a verdadero, THE Cotizador SHALL retornar la Cotizacion con su Identificador_Unico, fecha de creación, Subtotal, Recargo_Urgencia, Total y Tiempo_Estimado_Entrega ajustado.
