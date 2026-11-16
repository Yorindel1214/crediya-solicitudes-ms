package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.Solicitud;

/**
 * Contrato (puerto de salida): "alguien que sepa guardar solicitudes".
 */
public interface SolicitudRepository {
    Solicitud guardar(Solicitud solicitud);
}
