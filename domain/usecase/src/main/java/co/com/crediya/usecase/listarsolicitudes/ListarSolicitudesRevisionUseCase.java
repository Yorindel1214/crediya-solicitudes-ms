package co.com.crediya.usecase.listarsolicitudes;

import co.com.crediya.model.cliente.Cliente;
import co.com.crediya.model.cliente.gateways.ClienteGateway;
import co.com.crediya.model.comun.Pagina;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroSolicitudes;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.exceptions.ValidacionException;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipoprestamo.TipoPrestamo;
import co.com.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HU4: listado paginado y filtrable de las solicitudes que esperan la decisión del asesor
 * ("Pendiente de revisión", "Rechazada" y "Revisión manual").
 * <p>
 * Por cada solicitud devuelve: monto, plazo, email, nombre, tipo de préstamo, tasa de interés,
 * estado, salario base y la deuda total mensual del cliente por sus solicitudes APROBADAS.
 * <p>
 * Deuda mensual = suma de las cuotas fijas (sistema francés) de cada préstamo aprobado:
 * cuota = monto * i / (1 - (1 + i)^-n), con i = tasa anual / 12 / 100 y n = plazo en meses.
 */
@RequiredArgsConstructor
public class ListarSolicitudesRevisionUseCase {

    public static final int TAMANO_MAXIMO = 50;
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal DOCE = BigDecimal.valueOf(12);

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final ClienteGateway clienteGateway;

    public Pagina<SolicitudRevision> execute(FiltroSolicitudes filtro) {
        validar(filtro);
        Set<EstadoSolicitud> estados = filtro.estado() == null
                ? EstadoSolicitud.PARA_REVISION
                : Set.of(filtro.estado());

        Pagina<Solicitud> pagina = solicitudRepository.buscarPagina(filtro, estados);
        if (pagina.contenido().isEmpty()) {
            return pagina.mapear(s -> null);
        }

        Map<Long, TipoPrestamo> tipos = tipoPrestamoRepository.buscarTodos().stream()
                .collect(Collectors.toMap(TipoPrestamo::getId, Function.identity()));

        Set<String> emails = pagina.contenido().stream()
                .map(Solicitud::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Cliente> clientes = emails.isEmpty() ? Collections.emptyMap()
                : clienteGateway.buscarPorEmails(emails).stream()
                .collect(Collectors.toMap(Cliente::email, Function.identity(), (a, b) -> a));

        Map<String, BigDecimal> deudaPorEmail = emails.isEmpty() ? Collections.emptyMap()
                : deudaMensualDeAprobadas(emails, tipos);

        return pagina.mapear(s -> aRevision(s, tipos, clientes, deudaPorEmail));
    }

    private void validar(FiltroSolicitudes filtro) {
        if (filtro.pagina() < 0)
            throw new ValidacionException("La página debe ser mayor o igual a 0");
        if (filtro.tamano() < 1 || filtro.tamano() > TAMANO_MAXIMO)
            throw new ValidacionException("El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO);
        if (filtro.estado() != null && !EstadoSolicitud.PARA_REVISION.contains(filtro.estado()))
            throw new ValidacionException(
                    "Solo se pueden listar solicitudes en estado PENDIENTE_REVISION, RECHAZADA o REVISION_MANUAL");
    }

    private Map<String, BigDecimal> deudaMensualDeAprobadas(Set<String> emails, Map<Long, TipoPrestamo> tipos) {
        List<Solicitud> aprobadas = solicitudRepository.buscarPorEmailsYEstado(emails, EstadoSolicitud.APROBADA);
        return aprobadas.stream().collect(Collectors.groupingBy(
                Solicitud::getEmail,
                Collectors.reducing(BigDecimal.ZERO, s -> cuotaMensual(s, tipos), BigDecimal::add)));
    }

    private SolicitudRevision aRevision(Solicitud s, Map<Long, TipoPrestamo> tipos,
                                        Map<String, Cliente> clientes, Map<String, BigDecimal> deudaPorEmail) {
        TipoPrestamo tipo = tipos.get(s.getIdTipoPrestamo());
        Cliente cliente = s.getEmail() == null ? null : clientes.get(s.getEmail());
        return SolicitudRevision.builder()
                .id(s.getId())
                .monto(s.getMonto())
                .plazo(s.getPlazoMeses())
                .email(s.getEmail())
                .nombre(cliente == null ? null : cliente.nombreCompleto())
                .tipoPrestamo(tipo == null ? null : tipo.getNombre())
                .tasaInteres(tipo == null ? null : tipo.getTasaInteres())
                .estadoSolicitud(s.getEstado())
                .salarioBase(cliente == null ? null : cliente.salarioBase())
                .deudaTotalMensualSolicitudesAprobadas(
                        redondear(deudaPorEmail.getOrDefault(s.getEmail(), BigDecimal.ZERO)))
                .build();
    }

    /** Cuota fija mensual de un préstamo (sistema francés). */
    static BigDecimal cuotaMensual(Solicitud s, Map<Long, TipoPrestamo> tipos) {
        TipoPrestamo tipo = tipos.get(s.getIdTipoPrestamo());
        BigDecimal tasaAnual = tipo == null || tipo.getTasaInteres() == null ? BigDecimal.ZERO : tipo.getTasaInteres();
        return cuotaMensual(s.getMonto(), tasaAnual, s.getPlazoMeses());
    }

    static BigDecimal cuotaMensual(BigDecimal monto, BigDecimal tasaAnualPorcentaje, int plazoMeses) {
        BigDecimal i = tasaAnualPorcentaje.divide(CIEN, PRECISION).divide(DOCE, PRECISION);
        if (i.signum() == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), PRECISION);
        }
        BigDecimal factor = BigDecimal.ONE.add(i).pow(plazoMeses, PRECISION);
        BigDecimal denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, PRECISION));
        return monto.multiply(i, PRECISION).divide(denominador, PRECISION);
    }

    private static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
