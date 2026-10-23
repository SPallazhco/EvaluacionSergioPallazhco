CREATE TABLE cuenta (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    numero_cuenta  VARCHAR(10)    NOT NULL,
    titular        VARCHAR(100)   NOT NULL,
    tipo           VARCHAR(20)    NOT NULL,
    saldo          DECIMAL(15, 2) NOT NULL,
    estado         VARCHAR(20)    NOT NULL,
    fecha_creacion DATETIME(6)    NOT NULL,
    CONSTRAINT pk_cuenta PRIMARY KEY (id),
    CONSTRAINT uk_cuenta_numero_cuenta UNIQUE (numero_cuenta),
    CONSTRAINT ck_cuenta_numero_cuenta CHECK (numero_cuenta REGEXP '^[0-9]{10}$'),
    CONSTRAINT ck_cuenta_tipo CHECK (tipo IN ('AHORRO', 'CORRIENTE')),
    CONSTRAINT ck_cuenta_estado CHECK (estado IN ('ACTIVA', 'INACTIVA')),
    CONSTRAINT ck_cuenta_saldo CHECK (saldo >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
