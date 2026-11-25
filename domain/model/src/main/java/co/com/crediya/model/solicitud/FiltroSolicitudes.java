package co.com.crediya.model.solicitud;

import lombok.Builder;

/**
 * HU4: filtros del listado para revisión. Todos son opcionales salvo página y tamaño.
 *
 * @param estado         un estado concreto (si es null, se listan todos los que esperan revisión)
 * @param email          correo del cliente
 * @param idTipoPrestamo tipo de préstamo
 * @param pagina         número de página, empieza en 0
 * @param tamano         elementos por página
 */
@Builder
public record FiltroSolicitudes(EstadoSolicitud estado, String email, Long idTipoPrestamo, int pagina, int tamano) {
}
