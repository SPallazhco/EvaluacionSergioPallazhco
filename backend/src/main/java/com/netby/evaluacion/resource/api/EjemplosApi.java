package com.netby.evaluacion.resource.api;

final class EjemplosApi {

    static final String PROBLEM_JSON = "application/problem+json";

    static final String CREAR_CUENTA = """
            {
              "numeroCuenta": "1234567890",
              "titular": "Ana Pérez",
              "tipo": "AHORRO",
              "saldoInicial": 1500.00,
              "estado": "ACTIVA"
            }""";

    static final String CUENTA = """
            {
              "id": 1,
              "numeroCuenta": "1234567890",
              "titular": "Ana Pérez",
              "tipo": "AHORRO",
              "saldo": 1500.00,
              "estado": "ACTIVA",
              "fechaCreacion": "2026-10-24T15:30:00Z"
            }""";

    static final String RESUMEN_CUENTA = """
            {
              "cuentaId": 1,
              "numeroCuenta": "1234567890",
              "saldoActual": 13000.00,
              "totalTransferidoHoy": 7000.00,
              "montoDisponibleHoy": 3000.00,
              "cantidadTransferenciasHoy": 2,
              "cantidadSospechosasHoy": 1,
              "cantidadSospechosasTotal": 3
            }""";

    static final String CREAR_TRANSFERENCIA = """
            {
              "numeroCuentaOrigen": "1234567890",
              "numeroCuentaDestino": "0987654321",
              "monto": 250.00
            }""";

    static final String TRANSFERENCIA_COMPLETADA = """
            {
              "id": 10,
              "numeroCuentaOrigen": "1234567890",
              "numeroCuentaDestino": "0987654321",
              "monto": 250.00,
              "fecha": "2026-10-24T15:30:00Z",
              "estado": "COMPLETADA",
              "motivo": null
            }""";

    static final String TRANSFERENCIA_EN_REVISION = """
            {
              "id": 11,
              "numeroCuentaOrigen": "1234567890",
              "numeroCuentaDestino": "0987654321",
              "monto": 6000.00,
              "fecha": "2026-10-24T15:35:00Z",
              "estado": "EN_REVISION",
              "motivo": "Monto superior a 5000.00"
            }""";

    static final String ERROR_VALIDACION = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "La solicitud contiene datos inválidos",
              "codigo": "SOLICITUD_INVALIDA",
              "timestamp": "2026-10-24T15:30:00Z",
              "errores": [
                { "campo": "numeroCuenta", "mensaje": "El número de cuenta debe tener exactamente 10 dígitos" }
              ]
            }""";

    static final String ERROR_CUENTA_NO_ENCONTRADA = """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "No existe la cuenta 0987654321",
              "codigo": "CUENTA_NO_ENCONTRADA",
              "timestamp": "2026-10-24T15:30:00Z"
            }""";

    static final String ERROR_TRANSFERENCIA_NO_ENCONTRADA = """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "No existe una transferencia con id 99",
              "codigo": "TRANSFERENCIA_NO_ENCONTRADA",
              "timestamp": "2026-10-24T15:30:00Z"
            }""";

    static final String ERROR_CUENTA_DUPLICADA = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "Ya existe una cuenta con el número 1234567890",
              "codigo": "CUENTA_DUPLICADA",
              "timestamp": "2026-10-24T15:30:00Z"
            }""";

    static final String ERROR_SALDO_INSUFICIENTE = """
            {
              "type": "about:blank",
              "title": "Unprocessable Entity",
              "status": 422,
              "detail": "La cuenta origen no tiene saldo suficiente",
              "codigo": "SALDO_INSUFICIENTE",
              "timestamp": "2026-10-24T15:30:00Z"
            }""";

    static final String ERROR_CUENTA_INACTIVA = """
            {
              "type": "about:blank",
              "title": "Unprocessable Entity",
              "status": 422,
              "detail": "La cuenta destino no está activa",
              "codigo": "CUENTA_INACTIVA",
              "timestamp": "2026-10-24T15:30:00Z"
            }""";

    static final String ERROR_LIMITE_DIARIO = """
            {
              "type": "about:blank",
              "title": "Unprocessable Entity",
              "status": 422,
              "detail": "La transferencia supera el límite diario de 10000.00. Monto disponible hoy: 1000.00",
              "codigo": "LIMITE_DIARIO_EXCEDIDO",
              "timestamp": "2026-10-24T15:30:00Z",
              "detalles": { "limiteDiario": 10000.00, "montoDisponible": 1000.00 }
            }""";

    static final String ERROR_INTERNO = """
            {
              "type": "about:blank",
              "title": "Internal Server Error",
              "status": 500,
              "detail": "Ocurrió un error inesperado. Intente nuevamente más tarde",
              "codigo": "ERROR_INTERNO",
              "timestamp": "2026-10-24T15:30:00Z"
            }""";

    private EjemplosApi() {
    }
}
