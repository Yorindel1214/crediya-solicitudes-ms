-- HU4: migración para una base que ya existía desde la HU2/HU3.
-- (Una base nueva no la necesita: init.sql ya trae estas columnas y valores.)
-- Ejecutar:
--   docker exec -i postgres_solicitudes psql -U admin -d crediya_solicitudes < db/migracion-hu4.sql

ALTER TABLE tipos_prestamo ADD COLUMN IF NOT EXISTS tasa_interes NUMERIC(5,2);
ALTER TABLE solicitudes    ADD COLUMN IF NOT EXISTS email VARCHAR(150);

UPDATE tipos_prestamo SET tasa_interes = 24.00 WHERE nombre = 'Libre inversión' AND tasa_interes IS NULL;
UPDATE tipos_prestamo SET tasa_interes = 16.50 WHERE nombre = 'Vehículo'        AND tasa_interes IS NULL;
UPDATE tipos_prestamo SET tasa_interes = 12.00 WHERE nombre = 'Vivienda'        AND tasa_interes IS NULL;
UPDATE tipos_prestamo SET tasa_interes = 10.00 WHERE nombre = 'Educación'       AND tasa_interes IS NULL;
