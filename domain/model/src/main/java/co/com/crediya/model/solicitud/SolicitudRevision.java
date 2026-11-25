package co.com.crediya.model.solicitud;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * HU4: una fila del listado que ve el asesor. Junta datos de la solicitud, del tipo de préstamo
 * y del cliente (este último viene del microservicio de Autenticación).
 */
@Builder
public record SolicitudRevision(
        Long id,
        BigDecimal monto,
        Integer plazo,
        String email,
        String nombre,
        String tipoPrestamo,
        BigDecimal tasaInteres,
        EstadoSolicitud estadoSolicitud,
        BigDecimal salarioBase,
        BigDecimal deudaTotalMensualSolicitudesAprobadas) {
}
