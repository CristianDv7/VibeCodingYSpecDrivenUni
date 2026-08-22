# Cotizador de Reparación de Calzados

SPA implementada con HTML, CSS y JavaScript nativo (sin frameworks ni bundlers). Permite seleccionar un tipo de calzado, elegir una o más reparaciones y opcionalmente marcar urgencia para obtener una cotización calculada por el backend.

## Estructura del proyecto

```
cotizador-frontend/
├── index.html
├── css/
│   └── estilos.css
├── js/
│   ├── app.js      ← orquestador principal
│   ├── state.js    ← estado en memoria + observer
│   ├── api.js      ← adaptador HTTP hacia la API real
│   └── mock.js     ← drop-in replacement de api.js para desarrollo
└── tests/
    ├── unit/
    └── properties/
```

## Requisitos

- Navegador moderno con soporte para ES Modules (`type="module"`)
- Node.js 18+ (solo para correr los tests)

## Activar el modo mock (desarrollo sin backend)

Para trabajar sin un backend real, cambia **una única línea** en `js/app.js`:

```js
// Producción (línea activa por defecto):
import * as api from './api.js'

// Desarrollo con mock — cambiar solo esta línea:
// import * as api from './mock.js'
```

El módulo `mock.js` tiene la misma firma que `api.js` y simula latencia de red (400 ms por defecto). No requiere ningún otro cambio en el código.

## Tests

Instalar dependencias:

```bash
npm install
```

Ejecutar todos los tests una sola vez:

```bash
npm test
```

Ejecutar en modo watch (re-corre al guardar cambios):

```bash
npm run test:watch
```

Los tests incluyen:

- **Tests unitarios** (`tests/unit/`) — verifican comportamiento específico de cada módulo
- **Tests de propiedades** (`tests/properties/`) — usan fast-check para verificar invariantes universales del sistema
