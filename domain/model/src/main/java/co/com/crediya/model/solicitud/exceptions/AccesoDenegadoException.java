package co.com.crediya.model.solicitud.exceptions;

/**
 * El usuario inició sesión, pero no puede hacer esta acción (por su rol o porque no es el dueño).
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
