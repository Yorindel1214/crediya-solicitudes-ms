package co.com.crediya.model.solicitud.exceptions;

public class TipoPrestamoNoExisteException extends RuntimeException {
    public TipoPrestamoNoExisteException(Long idTipoPrestamo) {
        super("El tipo de préstamo con id " + idTipoPrestamo + " no existe");
    }
}
