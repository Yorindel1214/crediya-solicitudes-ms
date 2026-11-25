package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.comun.Pagina;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroSolicitudes;
import co.com.crediya.model.solicitud.Solicitud;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Contrato (puerto de salida): "alguien que sepa guardar y buscar solicitudes".
 */
public interface SolicitudRepository {
    Solicitud guardar(Solicitud solicitud);

    /** HU4: una página de solicitudes en alguno de los estados dados, aplicando los filtros opcionales. */
    Pagina<Solicitud> buscarPagina(FiltroSolicitudes filtro, Set<EstadoSolicitud> estados);

    /** HU4: solicitudes de varios clientes en un estado (para sumar la deuda de las aprobadas). */
    List<Solicitud> buscarPorEmailsYEstado(Collection<String> emails, EstadoSolicitud estado);
}
