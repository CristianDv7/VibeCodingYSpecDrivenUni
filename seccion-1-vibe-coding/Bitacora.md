# Bitácora de Sesión — Taller Vibe Coding con IA

**Proyecto:** Ambiente local de desarrollo con Docker Compose  
**Carpeta:** `seccion-1-vibe-coding`  
**Herramienta:** Kiro IDE (Vibe session)  
**Fecha de sesión:** 20 de agosto de 2026

---

## Índice

1. [Prompt 1 — Crear el ambiente base](#prompt-1)
2. [Prompt 2 — Healthcheck de MySQL](#prompt-2)
3. [Prompt 3 — Credenciales en .env](#prompt-3)
4. [Prompt 4 — MySQL solo en red interna](#prompt-4)
5. [Prompt 5 — Volumen de logs de Nginx](#prompt-5)
6. [Prompt 6 — Error: servicio "db" no encontrado](#prompt-6)
7. [Prompt 7 — Error: curl en PowerShell](#prompt-7)
8. [Prompt 8 — Error: cliente mysql no instalado](#prompt-8)
9. [Prompt 9 — Descripción de servicios](#prompt-9)
10. [Prompt 10 — Simulación de fallo por puerto ocupado](#prompt-10)
11. [Prompt 11 — Reflexión del taller](#prompt-11)
12. [Prompt 12 — Agregar Adminer](#prompt-12)
13. [Prompt 13 — Esta bitácora](#prompt-13)
14. [Estado final del proyecto](#estado-final)

---

## Prompt 1 — Crear el ambiente base {#prompt-1}

### Prompt enviado

> Necesito un ambiente local de desarrollo y pruebas con Docker Compose. Quiero un contenedor de
> MySQL 8 con una base de datos llamada "tallerdae", usuario y contraseña de desarrollo, y
> persistencia de datos con un volumen. También quiero un contenedor de Nginx que sirva una página
> estática simple de bienvenida en el puerto 8080, y que en el futuro pueda actuar como proxy hacia
> un backend. Todo debe poder levantarse con un solo comando y debe incluir un archivo .env para
> las credenciales.

### Acción ejecutada

Kiro generó la estructura completa del proyecto en una sola iteración:

**Archivos creados:**

- `.env`
- `.gitignore`
- `docker-compose.yml`
- `nginx/nginx.conf`
- `nginx/html/index.html`

### Contenido generado

**`.env`** — Variables de entorno con credenciales de desarrollo:
```env
# ============================================================
# Credenciales de desarrollo - NO usar en producción
# ============================================================

# MySQL
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=tallerdae
MYSQL_USER=devuser
MYSQL_PASSWORD=devpassword
MYSQL_PORT=3306

# Nginx
NGINX_PORT=8080
```

**`docker-compose.yml`** — Servicios MySQL 8 y Nginx con red interna, volumen persistente y healthcheck:
```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: tallerdae-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - tallerdae-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  nginx:
    image: nginx:1.25-alpine
    container_name: tallerdae-nginx
    restart: unless-stopped
    ports:
      - "${NGINX_PORT:-8080}:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/html:/usr/share/nginx/html:ro
    networks:
      - tallerdae-net
    depends_on:
      mysql:
        condition: service_healthy

volumes:
  mysql_data:
    driver: local

networks:
  tallerdae-net:
    driver: bridge
```

**`nginx/nginx.conf`** — Configuración con bloque de proxy comentado listo para activar:
```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout 65;

    # upstream backend {
    #     server backend:3000;
    # }

    server {
        listen 80;
        server_name localhost;

        location / {
            root   /usr/share/nginx/html;
            index  index.html;
            try_files $uri $uri/ =404;
        }

        # location /api/ {
        #     proxy_pass http://backend/;
        #     ...
        # }

        error_page 404 /index.html;
        error_page 500 502 503 504 /index.html;
    }
}
```

**`nginx/html/index.html`** — Página de bienvenida con tema oscuro y estado de servicios animado.

**`.gitignore`** — Excluye `.env` y datos de MySQL:
```
.env
mysql_data/
```

### Comando para levantar el ambiente
```bash
docker compose up -d
```

---

## Prompt 2 — Healthcheck de MySQL {#prompt-2}

### Prompt enviado

> Agrega un healthcheck a MySQL para que Nginx no arranque hasta que la base de datos esté lista.

### Respuesta

Kiro verificó el archivo y confirmó que el healthcheck ya estaba incluido desde el Prompt 1.

El archivo ya contenía en el servicio `mysql`:
```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 30s
```

Y en el servicio `nginx`:
```yaml
depends_on:
  mysql:
    condition: service_healthy
```

**No se modificó ningún archivo.**

> `condition: service_healthy` retiene Nginx hasta que MySQL supera el healthcheck.
> `start_period: 30s` le da tiempo a MySQL para inicializarse sin contar los primeros fallos como reintentos.

---

## Prompt 3 — Credenciales en .env {#prompt-3}

### Prompt enviado

> Mueve las credenciales de MySQL a un archivo .env y no las dejes escritas directamente en el
> docker-compose.yml

### Respuesta

Kiro verificó ambos archivos y confirmó que ya estaba implementado desde el Prompt 1.

- **`.env`** contiene los valores reales (`rootpassword`, `devuser`, etc.)
- **`docker-compose.yml`** solo tiene referencias `${VARIABLE}`, nunca valores en texto plano
- **`.gitignore`** ya excluye el `.env`

**No se modificó ningún archivo.**

> Docker Compose carga el `.env` automáticamente cuando está en el mismo directorio.
> Para verificar la interpolación: `docker compose config`

---

## Prompt 4 — MySQL solo en red interna {#prompt-4}

### Prompt enviado

> Expón MySQL únicamente dentro de la red interna de Docker, no lo publiques en el host

### Archivos modificados

- `docker-compose.yml` — reemplazado `ports` por `expose`
- `.env` — eliminada la variable `MYSQL_PORT`

### Cambio en `docker-compose.yml`

**Antes:**
```yaml
ports:
  - "${MYSQL_PORT:-3306}:3306"
```

**Después:**
```yaml
# Puerto expuesto solo dentro de la red interna (tallerdae-net).
# No se publica en el host intencionalmente.
expose:
  - "3306"
```

### Cambio en `.env`

**Eliminado:**
```env
MYSQL_PORT=3306
```

> `ports` mapea el puerto al host (accesible desde `localhost:3306`).
> `expose` solo lo documenta y lo hace visible dentro de la red Docker.
> Los contenedores en `tallerdae-net` siguen accediendo a MySQL con hostname `mysql` y puerto `3306`.

---

## Prompt 5 — Volumen de logs de Nginx {#prompt-5}

### Prompt enviado

> Agrega un volumen para los logs de Nginx que pueda revisar desde mi máquina

### Archivos modificados

- `docker-compose.yml` — agregado bind mount `./nginx/logs:/var/log/nginx`
- `.gitignore` — agregada exclusión de `nginx/logs/*.log`
- `nginx/logs/.gitkeep` — archivo creado para que Git rastree la carpeta vacía

### Cambio en `docker-compose.yml`

**Antes:**
```yaml
volumes:
  - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
  - ./nginx/html:/usr/share/nginx/html:ro
```

**Después:**
```yaml
volumes:
  - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
  - ./nginx/html:/usr/share/nginx/html:ro
  - ./nginx/logs:/var/log/nginx
```

### Cambio en `.gitignore`

**Agregado:**
```
# Logs de Nginx — solo para uso local
nginx/logs/*.log
```

> Se usó bind mount en lugar de volumen nombrado para que los archivos estén
> en una ruta conocida y sean abribles con cualquier editor sin comandos Docker.

Archivos generados por Nginx en `nginx/logs/`:
- `access.log` — cada request que llega
- `error.log` — errores y advertencias

Para seguir logs en tiempo real:
```powershell
Get-Content .\nginx\logs\access.log -Wait
```

---

## Prompt 6 — Error: servicio "db" no encontrado {#prompt-6}

### Prompt enviado (output de consola)

```
PS> docker compose logs db --tail=50
no such service: db
```

### Respuesta

El servicio se llama `mysql`, no `db`. Comandos correctos:

```bash
# Ver las últimas 50 líneas
docker compose logs mysql --tail=50

# Seguir logs en tiempo real
docker compose logs mysql -f
```

**No se modificó ningún archivo.**

---

## Prompt 7 — Error: curl en PowerShell {#prompt-7}

### Prompt enviado (output de consola)

```
PS> curl -I http://localhost:8080
curl : Cannot find drive. A drive with the name 'http' does not exist.
```

### Respuesta

En PowerShell, `curl` es un alias de `Invoke-WebRequest` y no acepta flags de curl real.

**Alternativas correctas:**

```powershell
# Solo el status code
Invoke-WebRequest -Uri http://localhost:8080 -Method HEAD | Select-Object StatusCode, StatusDescription

# Ver headers completos
(Invoke-WebRequest -Uri http://localhost:8080 -Method HEAD).Headers

# Usar curl real explícitamente
curl.exe -I http://localhost:8080
```

**No se modificó ningún archivo.**

---

## Prompt 8 — Error: cliente mysql no instalado {#prompt-8}

### Prompt enviado (output de consola)

```
PS> mysql -h 127.0.0.1 -P 3306 -u dev_user -p tallerdae
mysql : The term 'mysql' is not recognized as the name of a cmdlet...
```

### Respuesta

Dos causas simultáneas:
1. El cliente `mysql` no está instalado en el host.
2. MySQL no está publicado en el host (se quitó en el Prompt 4).

**Solución correcta — conectarse desde dentro del contenedor:**

```powershell
# Con el usuario de desarrollo
docker exec -it tallerdae-mysql mysql -u devuser -p tallerdae
# Contraseña: devpassword

# Como root
docker exec -it tallerdae-mysql mysql -u root -p
# Contraseña: rootpassword
```

**No se modificó ningún archivo.**

> Se mencionó también la posibilidad de crear un `docker-compose.override.yml`
> para publicar el puerto temporalmente sin modificar el compose principal,
> para uso con clientes gráficos como DBeaver o TablePlus.

---

## Prompt 9 — Descripción de servicios {#prompt-9}

### Prompt enviado

> Explícame en dos frases qué hace cada servicio de este docker-compose.yml, como si yo no lo
> hubiera pedido

### Respuesta

**mysql** — Levanta una base de datos MySQL 8 con una base llamada `tallerdae` y un usuario de
desarrollo, guardando los datos en un volumen para que no se pierdan al reiniciar. Solo es
accesible desde otros contenedores en la red interna, nunca desde tu máquina directamente.

**nginx** — Sirve una página HTML estática en `http://localhost:8080` y está preconfigurado para
actuar como proxy hacia un backend cuando lo agregues. Espera a que MySQL esté saludable antes
de arrancar.

**No se modificó ningún archivo.**

---

## Prompt 10 — Simulación de fallo por puerto ocupado {#prompt-10}

### Prompt enviado

> Simula que este ambiente falla al arrancar porque el puerto 3306 ya está ocupado; dime cómo lo
> solucionarías

### Respuesta

Este ambiente **no puede fallar por ese motivo** en MySQL, porque el puerto 3306 no está publicado
en el host (se quitó en el Prompt 4). El error `bind: address already in use` solo ocurre con `ports`.

El único servicio que podría fallar por puerto ocupado es **Nginx en el 8080**. El error se vería:
```
Error response from daemon: driver failed programming external connectivity:
Bind for 0.0.0.0:8080 failed: port is already allocated
```

Solución: cambiar `NGINX_PORT` en el `.env`:
```env
NGINX_PORT=8081
```

Si MySQL estuviera publicado y el 3306 estuviera ocupado:
```powershell
# Diagnóstico
netstat -ano | Select-String ":3306"
Get-Process -Id <PID>
```

Soluciones:
1. Detener el proceso local que ocupa el puerto.
2. Cambiar el mapeo en el override: `"3307:3306"`.

**No se modificó ningún archivo.**

---

## Prompt 11 — Reflexión del taller {#prompt-11}

### Prompt enviado

> Agrega un md con las respuestas a estas preguntas en la carpeta:
> 1. ¿Cuánto tiempo tomó desde el primer prompt hasta tener el ambiente funcionando?
> 2. ¿Revisaste línea por línea el docker-compose.yml generado, o confiaste en que "funcionaba"?
> 3. Si este mismo ambiente tuviera que promoverse a staging compartido...
> 4. ¿Qué credenciales o configuraciones sensibles quedaron expuestas?

### Archivo creado

- `reflexion.md`

**No se modificaron archivos existentes.**

---

## Prompt 12 — Agregar Adminer {#prompt-12}

### Prompt enviado

> Agregar Adminer administrar MySQL desde el navegador

### Archivos modificados

- `docker-compose.yml` — agregado servicio `adminer`
- `.env` — agregada variable `ADMINER_PORT=8081`

### Cambio en `.env`

**Agregado:**
```env
# Adminer
ADMINER_PORT=8081
```

### Cambio en `docker-compose.yml`

**Agregado:**
```yaml
adminer:
  image: adminer:4-standalone
  container_name: tallerdae-adminer
  restart: unless-stopped
  ports:
    - "${ADMINER_PORT:-8081}:8080"
  environment:
    ADMINER_DEFAULT_SERVER: mysql
  networks:
    - tallerdae-net
  depends_on:
    mysql:
      condition: service_healthy
```

### Comando para aplicar sin reiniciar todo

```bash
docker compose up -d adminer
```

### Datos de acceso a Adminer

URL: `http://localhost:8081`

| Campo | Valor |
|---|---|
| Sistema | MySQL |
| Servidor | `mysql` |
| Usuario | `devuser` |
| Contraseña | `devpassword` |
| Base de datos | `tallerdae` |

---

## Prompt 13 — Esta bitácora {#prompt-13}

### Prompt enviado

> Genera una Bitacora.md de todos los prompts enviados con su respuesta o con ejecución de consola,
> se debe detallar el paso a paso de lo hecho en esta sesión

### Archivo creado

- `Bitacora.md` (este archivo)

---

## Estado final del proyecto {#estado-final}

### Estructura de archivos

```
seccion-1-vibe-coding/
├── .env                        ← credenciales de desarrollo (ignorado por git)
├── .gitignore
├── docker-compose.yml
├── reflexion.md
├── Bitacora.md                 ← este archivo
└── nginx/
    ├── nginx.conf              ← configuración con proxy comentado
    ├── html/
    │   └── index.html          ← página de bienvenida
    └── logs/
        └── .gitkeep            ← carpeta rastreada por git, logs ignorados
```

### Servicios activos

| Servicio | Imagen | Puerto host | Puerto interno | Notas |
|---|---|---|---|---|
| mysql | mysql:8.0 | — | 3306 | Solo red interna |
| nginx | nginx:1.25-alpine | 8080 | 80 | Página estática + proxy ready |
| adminer | adminer:4-standalone | 8081 | 8080 | Admin web de MySQL |

### URLs de acceso

| Recurso | URL |
|---|---|
| Página de bienvenida | http://localhost:8080 |
| Adminer (admin DB) | http://localhost:8081 |

### Comandos esenciales

```bash
# Levantar todo
docker compose up -d

# Ver estado de los servicios
docker compose ps

# Ver logs de un servicio
docker compose logs mysql -f
docker compose logs nginx -f

# Conectarse a MySQL desde el contenedor
docker exec -it tallerdae-mysql mysql -u devuser -p tallerdae

# Detener el ambiente
docker compose down

# Detener y borrar volúmenes (elimina datos de MySQL)
docker compose down -v
```

### Evolución del setup — resumen de cambios por iteración

| Iteración | Cambio | Archivo(s) |
|---|---|---|
| 1 | Setup completo inicial | Todos |
| 2 | Confirmación: healthcheck ya existía | — |
| 3 | Confirmación: .env ya existía | — |
| 4 | MySQL removido del host (`ports` → `expose`) | `docker-compose.yml`, `.env` |
| 5 | Logs de Nginx como bind mount | `docker-compose.yml`, `.gitignore`, `nginx/logs/.gitkeep` |
| 6–10 | Correcciones de comandos en consola | — |
| 11 | Reflexión del taller | `reflexion.md` |
| 12 | Adminer agregado | `docker-compose.yml`, `.env` |
| 13 | Esta bitácora | `Bitacora.md` |
