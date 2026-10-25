# EvaluacionSergioPallazhco

Sistema de Gestión de Transferencias Interbancarias: administración de cuentas bancarias,
ejecución de transferencias con validaciones de negocio, detección automática de operaciones
sospechosas y consulta del historial de movimientos. Incluye una API REST en Quarkus y un
dashboard en Angular para visualizar cuentas y ejecutar transferencias.

## Contenido

- [Stack](#stack)
- [Ejecución rápida con Docker](#ejecución-rápida-con-docker)
- [Ejecución en modo desarrollo](#ejecución-en-modo-desarrollo)
- [Base de datos](#base-de-datos)
- [Compilación, pruebas y cobertura](#compilación-pruebas-y-cobertura)
- [Documentación de la API](#documentación-de-la-api)
- [Reglas de negocio y supuestos](#reglas-de-negocio-y-supuestos)
- [Arquitectura del backend](#arquitectura-del-backend)
- [Dashboard en Angular](#dashboard-en-angular)
- [Manejo de errores](#manejo-de-errores)
- [Observabilidad](#observabilidad)
- [Criterios de la evaluación](#criterios-de-la-evaluación)
- [Fuera del alcance](#fuera-del-alcance)

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Quarkus 3.33 LTS (Quarkus REST, Hibernate ORM con Panache, Hibernate Validator) |
| Frontend | Angular 22 (standalone, zoneless, signals), Angular Material 22, TypeScript 6 |
| Base de datos | MySQL 8.4, migraciones con Flyway |
| Mapeo de DTOs | MapStruct |
| Documentación API | OpenAPI 3 / Swagger UI |
| Pruebas | JUnit 5, Mockito, RestAssured, AssertJ, ArchUnit, JaCoCo (backend); Vitest (frontend) |
| Calidad del frontend | ESLint con typescript-eslint, angular-eslint y SonarJS; Prettier |
| Contenedores | Docker, Docker Compose, nginx |

```
EvaluacionSergioPallazhco/
├── backend/              API REST (Quarkus)
├── frontend/             Dashboard (Angular)
├── docker-compose.yml    MySQL + backend + frontend
└── .env.example          Variables configurables
```

## Ejecución rápida con Docker

Requisito: Docker con Docker Compose.

```bash
docker compose up -d --build
```

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/q/swagger-ui |
| Health check | http://localhost:8080/q/health |
| MySQL | localhost:3307 (base `transferencias`, usuario `transferencias`, clave `transferencias`) |

La primera construcción descarga dependencias y tarda unos minutos. Para detener el entorno:

```bash
docker compose down
```

Para borrar también los datos de MySQL usar `docker compose down -v`. Los valores por defecto
(credenciales, puerto de MySQL, puerto del frontend, origen permitido por CORS) se pueden cambiar
copiando `.env.example` como `.env`. Si se cambia `FRONTEND_PORT`, `CORS_ORIGINS` debe apuntar al
mismo puerto (por ejemplo `http://localhost:4300`).

## Ejecución en modo desarrollo

### Backend

Requisitos: Java 21 y Docker (Maven no es necesario, el proyecto incluye Maven Wrapper).

```bash
cd backend
./mvnw quarkus:dev
```

En modo desarrollo Quarkus levanta automáticamente un contenedor de MySQL 8.4 (Dev Services) y
aplica las migraciones; no hay que crear la base de datos. La API queda en http://localhost:8080
y Swagger UI en http://localhost:8080/q/swagger-ui.

Para usar una base de datos MySQL existente, ejecutar el jar con estas variables de entorno:

```bash
cd backend
./mvnw package -DskipTests
DB_URL=jdbc:mysql://localhost:3307/transferencias DB_USERNAME=transferencias DB_PASSWORD=transferencias java -jar target/quarkus-app/quarkus-run.jar
```

### Frontend

Requisitos: Node.js 24 LTS (versión fijada en `frontend/.nvmrc`; Angular 22 también admite
Node 22.22.3 o superior) y el backend en ejecución en http://localhost:8080, ya sea con
`./mvnw quarkus:dev` o con `docker compose up -d mysql backend`.

```bash
cd frontend
nvm use
npm ci
npm start
```

La aplicación queda en http://localhost:4200 y se recarga al guardar cambios. La URL de la API
se define en `frontend/src/environments/environment*.ts`.

## Base de datos

El esquema se crea con Flyway al iniciar la aplicación. Los scripts están en
`backend/src/main/resources/db/migration`:

| Script | Contenido |
|---|---|
| `V1__crear_tabla_cuenta.sql` | Tabla `cuenta` con unicidad del número, formato de 10 dígitos y saldo no negativo |
| `V2__crear_tabla_transferencia.sql` | Tabla `transferencia` con llaves foráneas, restricciones e índices de consulta |

Si se prefiere crear la base de datos manualmente:

```sql
CREATE DATABASE transferencias CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'transferencias'@'%' IDENTIFIED BY 'transferencias';
GRANT ALL PRIVILEGES ON transferencias.* TO 'transferencias'@'%';
```

Luego ejecutar los scripts `V1` y `V2` en orden, o simplemente iniciar la aplicación y Flyway los
aplicará. Hibernate no modifica el esquema, solo valida que coincida con las entidades.

Las fechas se almacenan en UTC.

## Compilación, pruebas y cobertura

### Backend

```bash
cd backend
./mvnw package          # compila, ejecuta las pruebas y genera target/quarkus-app
./mvnw test             # solo pruebas
```

Las pruebas de integración usan un MySQL real en Docker (Dev Services), por lo que Docker debe
estar en ejecución. El reporte de cobertura queda en `backend/target/jacoco-report/index.html`.

| Tipo de prueba | Qué valida |
|---|---|
| Unitarias (JUnit 5 + Mockito) | Servicios de negocio: detección de sospechosas, límite diario, transferencias, resumen |
| Integración (`@QuarkusTest`) | Endpoints contra MySQL real: códigos HTTP, validaciones, filtros, persistencia |
| Concurrencia | Transferencias simultáneas sobre la misma cuenta y transferencias cruzadas A→B / B→A |
| Arquitectura (ArchUnit) | Separación de capas, ausencia de ciclos, inyección por constructor |
| Documentación | Cada endpoint documenta sus respuestas en OpenAPI |

### Frontend

```bash
cd frontend
npm test                # pruebas unitarias con Vitest
npm run lint            # ESLint (TypeScript y plantillas)
npm run format:check    # formato con Prettier
npm run build           # build de producción en dist/transferencias-web
```

| Tipo de prueba | Qué valida |
|---|---|
| Servicios | Solicitudes HTTP, carga de todas las páginas de cuentas, cuerpo exacto de la transferencia |
| Interceptores | Cabecera `X-Request-Id` y conversión de errores RFC 7807 |
| Validaciones | Campos requeridos, monto numérico, mayor a cero y con 2 decimales, cuentas distintas |
| Componentes | Tabla y ordenamiento por saldo, formulario, mensajes del backend y refresco de saldos |

## Documentación de la API

Swagger UI: http://localhost:8080/q/swagger-ui — documento OpenAPI: http://localhost:8080/q/openapi

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/cuentas` | Registrar una cuenta | 201, 400, 409 |
| GET | `/api/cuentas?pagina=0&tamanio=20` | Listar cuentas (paginado) | 200, 400 |
| GET | `/api/cuentas/{id}` | Obtener una cuenta | 200, 404 |
| GET | `/api/cuentas/{id}/resumen` | Resumen del día de una cuenta | 200, 404 |
| POST | `/api/transferencias` | Ejecutar una transferencia | 201, 400, 404, 422 |
| GET | `/api/transferencias` | Historial con filtros (paginado) | 200, 400 |
| GET | `/api/transferencias/{id}` | Obtener una transferencia | 200, 404 |

Filtros del historial (todos opcionales): `cuentaOrigen`, `cuentaDestino` (número de cuenta),
`fechaDesde`, `fechaHasta` (`yyyy-MM-dd`, inclusivas), `estado` (`COMPLETADA`, `RECHAZADA`,
`EN_REVISION`), `pagina`, `tamanio`. Los resultados se ordenan por fecha descendente.

Ejemplo:

```bash
curl -X POST http://localhost:8080/api/cuentas -H 'Content-Type: application/json' \
  -d '{"numeroCuenta":"1111111111","titular":"Ana Pérez","tipo":"AHORRO","saldoInicial":20000,"estado":"ACTIVA"}'

curl -X POST http://localhost:8080/api/cuentas -H 'Content-Type: application/json' \
  -d '{"numeroCuenta":"2222222222","titular":"Luis Torres","tipo":"CORRIENTE","saldoInicial":0,"estado":"ACTIVA"}'

curl -X POST http://localhost:8080/api/transferencias -H 'Content-Type: application/json' \
  -d '{"numeroCuentaOrigen":"1111111111","numeroCuentaDestino":"2222222222","monto":250.00}'

curl 'http://localhost:8080/api/transferencias?cuentaOrigen=1111111111&estado=COMPLETADA'
```

## Reglas de negocio y supuestos

| Regla | Implementación |
|---|---|
| Cuenta | Número único de 10 dígitos, titular, tipo (`AHORRO`/`CORRIENTE`), saldo inicial ≥ 0, estado (`ACTIVA`/`INACTIVA`) |
| Transferencia válida | Ambas cuentas existen y están activas, son distintas, el monto es mayor a cero y la cuenta origen tiene saldo suficiente |
| Atomicidad | Débito, crédito y registro de la transferencia en una sola transacción |
| Operación sospechosa | Monto mayor a 5,000.00, o la cuenta origen ya realizó 5 transferencias en la última hora |
| Límite diario | Una cuenta no puede transferir más de 10,000.00 en un día calendario; el rechazo indica el monto disponible |

Supuestos tomados donde el enunciado es ambiguo:

- **Una transferencia sospechosa se ejecuta**: los saldos se actualizan y queda con estado
  `EN_REVISION` para revisión posterior. No se bloquea.
- **"Supera $5,000"** es estrictamente mayor: 5,000.00 no es sospechosa, 5,000.01 sí.
- **"Más de 5 transferencias en la última hora"** usa una ventana móvil de 60 minutos: con 5
  transferencias previas en esa ventana, la sexta se marca para revisión.
- **Día calendario** en la zona `America/Guayaquil`. Se puede transferir exactamente 10,000.00;
  10,000.01 se rechaza.
- **Qué cuenta para los acumulados**: las transferencias `COMPLETADA` y `EN_REVISION` (movieron
  dinero). Las `RECHAZADA` no cuentan para el límite diario, la regla por hora ni el resumen.
- **Transferencias rechazadas**: las que incumplen una regla de negocio (cuenta inactiva, saldo
  insuficiente, límite diario) se registran con estado `RECHAZADA` y su motivo, y se responde
  422. Las solicitudes mal formadas (400) o con cuentas inexistentes (404) no se registran porque
  no hay una transferencia válida que asociar.
- **Cuenta duplicada** responde 409 Conflict.
- **Resumen**: los valores del día se refieren a la cuenta como origen. Se informan las
  sospechosas del día y el total histórico, además del monto disponible del límite diario.
- **Identificadores**: las transferencias y los filtros usan el número de cuenta; el resumen usa
  el id interno, como indica el enunciado (`/api/cuentas/{id}/resumen`).

Los umbrales están en `application.properties` (`transferencias.*`) y pueden cambiarse sin
recompilar.

### Concurrencia

Al ejecutar una transferencia se bloquean ambas cuentas (`SELECT ... FOR UPDATE`) hasta el fin de
la transacción. Esto evita que dos transferencias simultáneas lean el mismo saldo o el mismo
acumulado diario. Las cuentas se bloquean siempre en el mismo orden (por número de cuenta) para
evitar deadlocks entre transferencias cruzadas. Ambos escenarios tienen pruebas con solicitudes
simultáneas reales.

## Arquitectura del backend

Arquitectura en capas: los Resources reciben la solicitud HTTP y delegan, los Services contienen
las reglas de negocio y las transacciones, y los Repositories acceden a datos.

```
com.netby.evaluacion
├── resource        Capa HTTP
│   ├── api         Contratos: rutas, validaciones y documentación OpenAPI
│   ├── handler     ExceptionMappers (respuestas de error)
│   ├── filter      Id de correlación por solicitud
│   └── converter   Conversión de parámetros de consulta
├── service         Reglas de negocio y transacciones
├── repository      Acceso a datos (Panache)
├── entity          Entidades JPA
├── dto             Objetos de entrada y salida de la API
├── mapper          Conversión entidad ↔ DTO (MapStruct)
├── exception       Excepciones de negocio
└── config          Configuración de reglas y reloj
```

| Archivo esperado | Ubicación |
|---|---|
| `CuentaResource`, `TransferenciaResource` | `resource/` (contratos en `resource/api/CuentaApi`, `TransferenciaApi`) |
| `CuentaService`, `TransferenciaService` | `service/` |
| `DeteccionSospechosaService`, `LimiteDiarioService` | `service/` |
| `CuentaRepository`, `TransferenciaRepository` | `repository/` |
| `Cuenta`, `Transferencia` | `entity/` |

Principios aplicados:

- Inyección de dependencias por constructor en todas las clases.
- Las entidades no salen de la capa de servicio: la API usa DTOs (records) mapeados con MapStruct.
- Los servicios no dependen de HTTP; las excepciones de negocio se traducen a códigos HTTP en
  `resource/handler`.
- La hora actual se obtiene de un `Clock` inyectable, por lo que las reglas basadas en tiempo se
  prueban de forma determinista.
- Las reglas de ArchUnit (`ArquitecturaTest`) hacen fallar el build si se rompe la separación de
  capas.

## Dashboard en Angular

Aplicación de una sola página con la tabla de cuentas y el formulario de transferencia.

### Funcionalidades

- **Tabla de cuentas** con número, titular, tipo, saldo y estado. Se puede ordenar por saldo de
  forma ascendente o descendente y paginar (10, 25 o 50 filas). Muestra estados de carga, vacío y
  error con opción de reintentar.
- **Formulario de transferencia** con selección de cuenta origen y destino, monto y botón de
  envío. Valida en el cliente que todos los campos sean obligatorios, que el monto sea numérico,
  mayor a cero y con hasta 2 decimales (acepta coma o punto), y que la cuenta destino sea
  distinta de la origen. Las cuentas inactivas aparecen deshabilitadas.
- **Resultado de la transferencia**: mensaje de éxito, aviso cuando queda `EN_REVISION` (con el
  motivo) o el error devuelto por el backend con su código de seguimiento (`X-Request-Id`). Tras
  una transferencia exitosa se limpia el formulario y se actualizan los saldos de la tabla.
- **Resumen de la cuenta origen** (`GET /api/cuentas/{id}/resumen`): saldo, monto disponible del
  límite diario, total transferido y transferencias del día.
- Montos en dólares con formato `es-EC` (`$1.500,00`).

### Estructura

```
frontend/src/app
├── core            Configuración (URL de la API, localización), interceptores HTTP, modelo de errores
├── shared          Modelos y componentes reutilizables (paginación, mensajes, tipado de celdas)
└── features
    ├── cuentas         cuentas.component, cuenta.service, resumen-cuenta.component
    ├── transferencias  transferencia-form.component, transferencia.service, validadores
    └── dashboard       Página que compone la tabla y el formulario (carga diferida)
```

| Archivo esperado | Ubicación |
|---|---|
| `cuentas.component.ts`, `cuentas.component.html`, `cuenta.service.ts` | `frontend/src/app/features/cuentas/` |
| `transferencia-form.component.ts`, `transferencia-form.component.html`, `transferencia.service.ts` | `frontend/src/app/features/transferencias/` |

### Decisiones de diseño

- **Componentes standalone, zoneless y `OnPush`**, con signals para el estado y `rxResource` para
  las consultas. No hay suscripciones manuales sin liberar.
- **Inyección con `inject()`**, como recomienda la guía de estilo de Angular; ESLint lo exige.
- **Modelos tipados** que reflejan los DTOs del backend con los mismos nombres
  (`CuentaResponse`, `CrearTransferenciaRequest`, `ProblemaResponse`, etc.).
- **Listado completo de cuentas**: `GET /api/cuentas` es paginado (máximo 100 por página). El
  servicio pide la primera página y, si hay más, el resto en paralelo; el ordenamiento por saldo
  y la paginación de la tabla se hacen en el cliente sobre todas las cuentas, de modo que el
  orden es correcto en todas las páginas.
- **Manejo centralizado de errores HTTP**: un interceptor convierte cualquier error en `ErrorApi`
  (código, mensaje, errores por campo, detalles y `X-Request-Id`); otro interceptor envía un
  `X-Request-Id` por solicitud para correlacionar la pantalla con los logs del backend.
- **Validaciones del cliente sin duplicar reglas de negocio**: el cliente valida formato y
  obligatoriedad; saldo, estado de las cuentas y límite diario los decide el backend.
- **El backend rechaza propiedades desconocidas**, por lo que el servicio de transferencias arma
  el cuerpo solo con los campos del contrato.
- **URL de la API por entorno** mediante el token `API_URL`. Tanto en desarrollo como en Docker
  el navegador llama a `http://localhost:8080/api` usando CORS.
- **Imagen Docker multi-stage**: se compila con Node 24 y se sirve con nginx sin privilegios, con
  fallback de SPA, caché inmutable para los archivos con hash, gzip y cabeceras de seguridad.

## Manejo de errores

Los errores siguen el formato RFC 7807 (`application/problem+json`) e incluyen un código estable:

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "La transferencia supera el límite diario de 10000.00. Monto disponible hoy: 1000.00",
  "codigo": "LIMITE_DIARIO_EXCEDIDO",
  "timestamp": "2026-10-24T15:30:00Z",
  "detalles": { "limiteDiario": 10000.00, "montoDisponible": 1000.00 }
}
```

| HTTP | Códigos |
|---|---|
| 400 | `SOLICITUD_INVALIDA` (con detalle por campo en `errores`) |
| 404 | `CUENTA_NO_ENCONTRADA`, `TRANSFERENCIA_NO_ENCONTRADA`, `RECURSO_NO_ENCONTRADO` |
| 409 | `CUENTA_DUPLICADA`, `CONFLICTO_DE_DATOS` |
| 422 | `CUENTA_INACTIVA`, `SALDO_INSUFICIENTE`, `LIMITE_DIARIO_EXCEDIDO` |
| 500 | `ERROR_INTERNO` (sin detalles internos; el error completo queda en el log) |

## Observabilidad

- Logs en formato JSON en producción y en texto legible en desarrollo.
- Cada respuesta incluye la cabecera `X-Request-Id`, que también aparece en todas las líneas de
  log de esa solicitud. Si el cliente envía un `X-Request-Id` válido, se reutiliza.
- Los números de cuenta se enmascaran en los logs (`******7890`).
- Health checks en `/q/health` (incluye la conexión a la base de datos).
- CORS habilitado solo para el origen del frontend (`http://localhost:4200` por defecto,
  configurable con `CORS_ORIGINS`).
- El frontend genera el `X-Request-Id` de cada solicitud y lo muestra como código de seguimiento
  cuando una transferencia falla, para ubicar la solicitud en los logs.

## Criterios de la evaluación

| Criterio | Dónde |
|---|---|
| CA-01 Registro y listado de cuentas | `CuentaApi`, `CuentaResource`, `CuentaService`, `Cuenta` |
| CA-02 Transferencias con validaciones | `TransferenciaApi`, `TransferenciaResource`, `TransferenciaService`, `Transferencia` |
| CA-03 Detección de operaciones sospechosas | `DeteccionSospechosaService` |
| CA-04 Límite diario | `LimiteDiarioService` |
| CA-05 Historial y resumen | `GET /api/transferencias`, `ResumenCuentaService` |
| CA-06 Dashboard de cuentas | `cuentas.component.ts/.html`, `cuenta.service.ts` |
| CA-07 Formulario de transferencia | `transferencia-form.component.ts/.html`, `transferencia.service.ts`, `transferencia.validators.ts` |
| CA-08 Arquitectura en capas | Estructura de paquetes y `ArquitecturaTest`; en el frontend `core` / `shared` / `features` |
| CA-09 Buenas prácticas | DTOs, MapStruct, ExceptionMappers centralizados, inyección por constructor; en el frontend interceptor de errores, modelos tipados y ESLint |
| CA-10 Documentación | Swagger UI y este README |

Extras implementados: Docker Compose (MySQL, backend y frontend), pruebas unitarias,
pruebas de integración, paginación, DTOs con MapStruct, logs estructurados y CORS.

## Fuera del alcance

No se implementaron, pero serían necesarios en un entorno productivo:

- Autenticación y autorización de los endpoints.
- Idempotencia de transferencias (cabecera `Idempotency-Key`) para evitar duplicados por reintentos.
- Flujo de aprobación o rechazo de las transferencias en revisión.
- Restricción de Swagger UI en producción.
- Paginación y ordenamiento en el servidor para `GET /api/cuentas` (por ejemplo `?orden=saldo,desc`)
  si el número de cuentas crece; hoy el frontend carga todas las cuentas y ordena en el cliente.
- Búsqueda dentro de los selectores de cuentas del formulario.
- Política `Content-Security-Policy` en nginx.
- Generación del `X-Request-Id` cuando el frontend se sirve sin HTTPS desde un host distinto de
  `localhost` (`crypto.randomUUID()` requiere un contexto seguro).
