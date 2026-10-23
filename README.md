# EvaluacionSergioPallazhco

Sistema de Gestión de Transferencias Interbancarias: administración de cuentas bancarias,
ejecución de transferencias con validaciones de negocio, detección automática de operaciones
sospechosas y consulta del historial de movimientos.

> 🚧 Proyecto en construcción. Las instrucciones completas de configuración, compilación y
> ejecución se documentarán al finalizar el desarrollo.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Quarkus 3 (REST, Hibernate ORM con Panache) |
| Base de datos | MySQL 8 (migraciones con Flyway) |
| Frontend | Angular + Angular Material |
| Documentación API | OpenAPI / Swagger UI |
| Calidad | JUnit 5, Mockito, ArchUnit, JaCoCo, SonarQube |

## Estructura del repositorio

```
EvaluacionSergioPallazhco/
├── backend/    API REST con Quarkus (arquitectura por capas)
└── frontend/   Dashboard operativo con Angular
```
