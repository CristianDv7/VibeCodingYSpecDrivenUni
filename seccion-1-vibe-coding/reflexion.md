# Reflexión — Taller Vibe Coding con IA

## 1. Tiempo e iteraciones para tener el ambiente funcionando

El ambiente base estuvo listo en una sola iteración conversacional: un prompt describiendo los requisitos
(MySQL 8, Nginx, volumen, `.env`, puerto 8080) fue suficiente para generar el `docker-compose.yml`,
la configuración de Nginx, la página de bienvenida y el `.gitignore`. 

En tiempo de reloj: menos de 10 minutos desde el primer prompt hasta un ambiente levantable con
`docker compose up -d`. Con un flujo manual habitual ese mismo resultado tomaría 30-60 minutos
considerando documentación, prueba y error, y ajustes de configuración.

---

## 2. Revisión del docker-compose.yml: ¿línea por línea o confianza ciega?

Cuando funciona al primer intento y se tiene confianza ciega.

Implica riesgos concretos:

- **Seguridad**: una imagen desactualizada, un puerto expuesto innecesariamente, o credenciales
  en texto plano pueden pasar desapercibidos. 
- **Comportamiento inesperado**: el `depends_on` sin `condition: service_healthy` solo espera que
  el contenedor arranque, no que MySQL esté listo. Si no se lee con atención, se asume que la
  dependencia está bien resuelta cuando no lo está.
- **Deuda técnica invisible**: configuraciones comentadas (el bloque de proxy en `nginx.conf`) o
  valores por defecto implícitos (`${NGINX_PORT:-8080}`) pueden olvidarse y causar sorpresas al
  escalar.

---

## 3. Promoción a staging compartido y la matriz de decisión

Mover este ambiente a staging implica un cambio de categoría en la matriz de decisión:
de decisiones locales y reversibles a decisiones con impacto en otros miembros del equipo y
potencialmente en datos reales.

Usar el mismo flujo de vibe coding sin fricción en staging es el error más común al escalar estos ambientes.

---

## 4. Credenciales expuestas y gobierno en contexto empresarial

Durante el ejercicio las credenciales quedaron en texto plano en el `.env`:

El `.gitignore` evita que lleguen al repositorio, pero eso no es suficiente en un contexto real:

- Cualquier persona con acceso al sistema de archivos del desarrollador puede leerlas.
- Si el `.gitignore` falla o se omite por error, las credenciales quedan expuestas en el historial
  de Git de forma permanente (un `git rm` no las elimina del historial).

**Cómo gobernarlas en una empresa:**

1. **Secrets manager**: AWS Secrets Manager, HashiCorp Vault o Azure Key Vault como fuente de
   verdad. Las aplicaciones obtienen las credenciales en runtime, nunca las almacenan en disco.
2. **Variables de entorno inyectadas por CI/CD**: GitHub Actions Secrets, GitLab CI Variables, etc.
   El pipeline las inyecta al momento del deploy sin que el desarrollador las vea.

El `.env` con contraseñas en texto plano es aceptable como convención de desarrollo local
únicamente. En cualquier entorno compartido o automatizado es una práctica que no debe tolerarse.
