package co.com.crediya.model.solicitud.exceptions;

/**
 * Otro microservicio del que dependemos no respondió o respondió con error.
 */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
