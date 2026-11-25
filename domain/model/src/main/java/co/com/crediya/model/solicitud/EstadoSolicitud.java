package co.com.crediya.model.solicitud;

import java.util.Set;

/**
 * Estados por los que pasa una solicitud. Toda solicitud nueva empieza en PENDIENTE_REVISION.
 */
public enum EstadoSolicitud {
    PENDIENTE_REVISION("Pendiente de revisión"),
    REVISION_MANUAL("Revisión manual"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");

    /** HU4: estados que esperan la decisión final del asesor. */
    public static final Set<EstadoSolicitud> PARA_REVISION = Set.of(PENDIENTE_REVISION, RECHAZADA, REVISION_MANUAL);

    private final String descripcion;

    EstadoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
