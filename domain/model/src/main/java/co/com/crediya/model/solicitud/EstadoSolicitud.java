package co.com.crediya.model.solicitud;

/**
 * Estados por los que pasa una solicitud. Toda solicitud nueva empieza en PENDIENTE_REVISION.
 */
public enum EstadoSolicitud {
    PENDIENTE_REVISION("Pendiente de revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");

    private final String descripcion;

    EstadoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
