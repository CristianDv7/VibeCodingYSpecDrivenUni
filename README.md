# Taller DAE — Sección 1: Despliegue Integrado

Stack completo con MySQL, Spring Boot y Nginx levantado con un solo comando.

## Requisitos

- Docker Desktop instalado y corriendo
- Puertos `80` y `8081` libres en el host

## Pasos para levantar el sistema

### 1. Configurar variables de entorno

```bash
cp .env.example .env
```

Edita `.env` si necesitas cambiar contraseñas o puertos. Los valores por defecto funcionan para desarrollo local.

### 2. Construir y levantar

Primera vez (compila el JAR del backend dentro de Docker):

```bash
docker compose up --build -d
```

Las siguientes veces (sin recompilar):

```bash
docker compose up -d
```

### 3. Verificar que todo esté corriendo

```bash
docker compose ps
```

Deberías ver los cuatro servicios en estado `Up`:

| Servicio | Puerto host |
|---|---|
| `tallerdae-nginx` | `80` |
| `tallerdae-mysql` | — (solo red interna) |
| `tallerdae-backend` | — (solo red interna) |
| `tallerdae-adminer` | `8081` |

## URLs de acceso

| Qué | URL |
|---|---|
| Frontend + API | http://localhost |
| Administrador MySQL | http://localhost:8081 |

> El backend **no tiene puerto publicado al host**. Todo el tráfico pasa por Nginx.

## Detener el sistema

```bash
docker compose down
```

Para detener y eliminar también los datos de MySQL:

```bash
docker compose down -v
```

## Estructura relevante

```
seccion-1-vibe-coding/
├── docker-compose.yml       # Definición del stack
├── .env                     # Variables de entorno (no se sube al repo)
├── .env.example             # Plantilla de variables
└── nginx/
    └── nginx.conf           # Proxy /api/ → backend, estáticos → frontend
```

Los archivos fuente del backend y frontend se encuentran en:

```
seccion-2-spec-driven-back/   # Spring Boot — Java 21
seccion-2-spec-driven-front/cotizador-frontend/  # HTML/CSS/JS
```
