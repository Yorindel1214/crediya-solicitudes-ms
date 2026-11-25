-- Base de datos del microservicio de Solicitudes (HU2).
-- Docker ejecuta este archivo solo la primera vez que crea la base.

-- Catálogo de tipos de préstamo que ofrece CrediYa.
-- La HU2 exige que la solicitud use uno de estos tipos.
CREATE TABLE IF NOT EXISTS tipos_prestamo (
    id          BIGSERIAL    PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

INSERT INTO tipos_prestamo (nombre, descripcion) VALUES
    ('Libre inversión', 'Para cualquier propósito personal'),
    ('Vehículo',        'Compra de carro o moto'),
    ('Vivienda',        'Compra o mejora de vivienda'),
    ('Educación',       'Matrículas y estudios')
ON CONFLICT (nombre) DO NOTHING;

-- Solicitudes de préstamo enviadas por los clientes.
CREATE TABLE IF NOT EXISTS solicitudes (
    id                  BIGSERIAL     PRIMARY KEY,
    documento_identidad VARCHAR(20)   NOT NULL,
    monto               NUMERIC(15,2) NOT NULL,
    plazo_meses         INTEGER       NOT NULL,
    id_tipo_prestamo    BIGINT        NOT NULL REFERENCES tipos_prestamo (id),
    estado              VARCHAR(30)   NOT NULL,
    fecha_creacion      TIMESTAMP     NOT NULL,
    email               VARCHAR(150)              -- HU3: correo del cliente (sale del token)
);
