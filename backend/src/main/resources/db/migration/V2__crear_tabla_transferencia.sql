CREATE TABLE transferencia (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    cuenta_origen_id  BIGINT         NOT NULL,
    cuenta_destino_id BIGINT         NOT NULL,
    monto             DECIMAL(15, 2) NOT NULL,
    fecha             DATETIME(6)    NOT NULL COMMENT 'UTC',
    estado            VARCHAR(20)    NOT NULL,
    motivo            VARCHAR(255)   NULL,
    CONSTRAINT pk_transferencia PRIMARY KEY (id),
    CONSTRAINT fk_transferencia_cuenta_origen FOREIGN KEY (cuenta_origen_id) REFERENCES cuenta (id),
    CONSTRAINT fk_transferencia_cuenta_destino FOREIGN KEY (cuenta_destino_id) REFERENCES cuenta (id),
    CONSTRAINT ck_transferencia_monto CHECK (monto > 0),
    CONSTRAINT ck_transferencia_cuentas_distintas CHECK (cuenta_origen_id <> cuenta_destino_id),
    CONSTRAINT ck_transferencia_estado CHECK (estado IN ('COMPLETADA', 'RECHAZADA', 'EN_REVISION'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Límite diario, regla por hora, resumen y filtro por origen
CREATE INDEX idx_transferencia_origen_fecha ON transferencia (cuenta_origen_id, fecha);

-- Filtro por destino
CREATE INDEX idx_transferencia_destino_fecha ON transferencia (cuenta_destino_id, fecha);

-- Historial ordenado por fecha
CREATE INDEX idx_transferencia_fecha ON transferencia (fecha);
