---
inclusion: always
---

## Arquitectura Hexagonal — Reglas de Dependencia

El backend sigue una arquitectura hexagonal (ports & adapters) con tres capas concéntricas. **Las dependencias siempre apuntan hacia el dominio; nunca al revés.**

### Capas

| Capa | Contenido | Restricciones |
|---|---|---|
| `domain` | Entidades, Value Objects, excepciones de negocio | Sin imports a frameworks, Spring, JPA ni infraestructura |
| `application` | Puertos de entrada (`UseCase`), puertos de salida (`RepositoryPort`), servicios (`Service`) | Solo depende de `domain` y sus propios puertos |
| `infrastructure` | Controladores REST, DTOs, Mappers, adaptadores de persistencia, configuración de Spring | Depende de `application` y `domain`; nunca al revés |

### Dependencias permitidas

```text
infrastructure.adapter.in.rest
    └──> application.port.in
            └──> domain

infrastructure.adapter.out.persistence
    └──> application.port.out
            └──> domain

application.service
    ├──> application.port.in
    ├──> application.port.out
    └──> domain
```

### Reglas clave para el asistente

1. **Nunca coloques** anotaciones de Spring (`@Entity`, `@RestController`, `@Repository`) dentro de `domain` o `application`.
2. **Nunca importes** clases de `infrastructure` desde `domain` o `application`.
3. Los servicios de aplicación (`*Service`) implementan un puerto de entrada (`*UseCase`) e inyectan puertos de salida (`*RepositoryPort`) — nunca repositorios JPA directamente.
4. Los DTOs (`*Request`, `*Response`) y mappers (`*Mapper`) viven exclusivamente en `infrastructure.adapter.in.rest`.
5. Cada nueva funcionalidad sigue el flujo: `Controller → UseCase (port) → Service → RepositoryPort → Adapter`.
6. El paquete raíz es `com.tallerdae.cotizador`; respeta esta estructura al crear nuevos archivos.
