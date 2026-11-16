package co.com.crediya.usecase.registrarsolicitud;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exceptions.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.exceptions.ValidacionException;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipoprestamo.TipoPrestamo;
import co.com.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * HU2: registrar una solicitud de préstamo.
 * 1) valida los datos del cliente y del préstamo,
 * 2) verifica que el tipo de préstamo exista,
 * 3) guarda la solicitud con estado inicial "Pendiente de revisión".
 */
@RequiredArgsConstructor
public class RegistrarSolicitudUseCase {

    private static final Pattern DOCUMENTO = Pattern.compile("^\\d{6,15}$");
    private static final int PLAZO_MAXIMO_MESES = 360;

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    public Solicitud execute(Solicitud solicitud) {
        validar(solicitud);

        TipoPrestamo tipo = tipoPrestamoRepository.buscarPorId(solicitud.getIdTipoPrestamo())
                .orElseThrow(() -> new TipoPrestamoNoExisteException(solicitud.getIdTipoPrestamo()));

        Solicitud nueva = solicitud.toBuilder()
                .documentoIdentidad(solicitud.getDocumentoIdentidad().trim())
                .estado(EstadoSolicitud.PENDIENTE_REVISION)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Solicitud guardada = solicitudRepository.guardar(nueva);
        return guardada.toBuilder().nombreTipoPrestamo(tipo.getNombre()).build();
    }

    private void validar(Solicitud s) {
        // Información del cliente
        if (s.getDocumentoIdentidad() == null || s.getDocumentoIdentidad().isBlank())
            throw new ValidacionException("El campo documento_identidad es obligatorio");
        if (!DOCUMENTO.matcher(s.getDocumentoIdentidad().trim()).matches())
            throw new ValidacionException("El documento de identidad debe tener solo números, entre 6 y 15 dígitos");

        // Información del préstamo
        if (s.getMonto() == null)
            throw new ValidacionException("El campo monto es obligatorio");
        if (s.getMonto().compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidacionException("El monto debe ser mayor que 0");
        if (s.getPlazoMeses() == null)
            throw new ValidacionException("El campo plazo_meses es obligatorio");
        if (s.getPlazoMeses() < 1 || s.getPlazoMeses() > PLAZO_MAXIMO_MESES)
            throw new ValidacionException("El plazo debe estar entre 1 y " + PLAZO_MAXIMO_MESES + " meses");
        if (s.getIdTipoPrestamo() == null)
            throw new ValidacionException("El campo id_tipo_prestamo es obligatorio");
    }
}
