package co.com.crediya.api;

import co.com.crediya.model.solicitud.exceptions.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exceptions.ServicioNoDisponibleException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import co.com.crediya.model.solicitud.exceptions.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.exceptions.ValidacionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Convierte cualquier error en una respuesta JSON clara: {"error": "mensaje"}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, String>> validacion(ValidacionException e) {
        log.warn("Validación fallida: {}", e.getMessage());
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(TipoPrestamoNoExisteException.class)
    public ResponseEntity<Map<String, String>> tipoNoExiste(TipoPrestamoNoExisteException e) {
        log.warn("Tipo de préstamo inválido: {}", e.getMessage());
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(AccesoDenegadoException e) {
        log.warn("Acceso denegado: {}", e.getMessage());
        return respuesta(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<Map<String, String>> servicioNoDisponible(ServicioNoDisponibleException e) {
        log.error("Servicio externo no disponible: {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(MethodArgumentTypeMismatchException e) {
        log.warn("Parámetro con formato inválido: {}={}", e.getName(), e.getValue());
        return respuesta(HttpStatus.BAD_REQUEST, "El parámetro " + e.getName() + " tiene un formato inválido");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> cuerpoInvalido(HttpMessageNotReadableException e) {
        log.warn("Cuerpo de petición inválido: {}", e.getMessage());
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o tiene un formato incorrecto");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        log.warn("Método HTTP no permitido: {}", e.getMethod());
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "El método " + e.getMethod() + " no está permitido en esta ruta");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> formatoNoSoportado(HttpMediaTypeNotSupportedException e) {
        log.warn("Tipo de contenido no soportado: {}", e.getContentType());
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El formato de la petición no es soportado. Envíe los datos en formato JSON");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> rutaNoEncontrada(NoResourceFoundException e) {
        log.warn("Ruta no encontrada: {}", e.getResourcePath());
        return respuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generico(Exception e) {
        log.error("Error inesperado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intente nuevamente más tarde");
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
