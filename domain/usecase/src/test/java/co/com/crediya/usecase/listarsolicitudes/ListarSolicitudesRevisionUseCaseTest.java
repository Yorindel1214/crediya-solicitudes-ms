package co.com.crediya.usecase.listarsolicitudes;

import co.com.crediya.model.cliente.Cliente;
import co.com.crediya.model.comun.Pagina;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroSolicitudes;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.exceptions.ValidacionException;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipoprestamo.TipoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListarSolicitudesRevisionUseCaseTest {

    private static final String ANA = "ana@correo.com";

    private final List<Solicitud> solicitudes = new ArrayList<>();
    private Set<EstadoSolicitud> estadosConsultados;
    private ListarSolicitudesRevisionUseCase useCase;

    @BeforeEach
    void setUp() {
        SolicitudRepository repositorio = new SolicitudRepository() {
            @Override
            public Solicitud guardar(Solicitud solicitud) {
                return solicitud;
            }

            @Override
            public Pagina<Solicitud> buscarPagina(FiltroSolicitudes filtro, Set<EstadoSolicitud> estados) {
                estadosConsultados = estados;
                List<Solicitud> filtradas = solicitudes.stream().filter(s -> estados.contains(s.getEstado())).toList();
                return new Pagina<>(filtradas, filtro.pagina(), filtro.tamano(), filtradas.size());
            }

            @Override
            public List<Solicitud> buscarPorEmailsYEstado(Collection<String> emails, EstadoSolicitud estado) {
                return solicitudes.stream()
                        .filter(s -> emails.contains(s.getEmail()) && s.getEstado() == estado).toList();
            }
        };
        TipoPrestamo libre = TipoPrestamo.builder().id(1L).nombre("Libre inversión")
                .tasaInteres(new BigDecimal("24.00")).build();
        useCase = new ListarSolicitudesRevisionUseCase(
                repositorio,
                new co.com.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository() {
                    @Override
                    public Optional<TipoPrestamo> buscarPorId(Long id) {
                        return Optional.of(libre);
                    }

                    @Override
                    public List<TipoPrestamo> buscarTodos() {
                        return List.of(libre);
                    }
                },
                emails -> List.of(Cliente.builder().email(ANA).nombres("Ana").apellidos("Pérez")
                        .salarioBase(new BigDecimal("4000000")).build()));

        solicitudes.add(solicitud(1L, EstadoSolicitud.PENDIENTE_REVISION));
        solicitudes.add(solicitud(2L, EstadoSolicitud.APROBADA));
        solicitudes.add(solicitud(3L, EstadoSolicitud.REVISION_MANUAL));
    }

    private static Solicitud solicitud(long id, EstadoSolicitud estado) {
        return Solicitud.builder().id(id).email(ANA).monto(new BigDecimal("5000000")).plazoMeses(24)
                .idTipoPrestamo(1L).estado(estado).build();
    }

    private static FiltroSolicitudes.FiltroSolicitudesBuilder filtro() {
        return FiltroSolicitudes.builder().pagina(0).tamano(10);
    }

    @Test
    void sinFiltroDeEstadoListaLosTresEstadosDeRevision() {
        Pagina<SolicitudRevision> pagina = useCase.execute(filtro().build());

        assertEquals(EstadoSolicitud.PARA_REVISION, estadosConsultados);
        assertEquals(2, pagina.contenido().size()); // la APROBADA no aparece
    }

    @Test
    void completaDatosDelClienteTipoYDeudaDeLasAprobadas() {
        SolicitudRevision fila = useCase.execute(filtro().build()).contenido().get(0);

        assertEquals("Ana Pérez", fila.nombre());
        assertEquals(new BigDecimal("4000000"), fila.salarioBase());
        assertEquals("Libre inversión", fila.tipoPrestamo());
        assertEquals(new BigDecimal("24.00"), fila.tasaInteres());
        // Una aprobada de 5.000.000 a 24 meses al 24 % anual (2 % mensual): cuota ≈ 264.355
        BigDecimal deuda = fila.deudaTotalMensualSolicitudesAprobadas();
        assertTrue(deuda.compareTo(new BigDecimal("264300")) > 0 && deuda.compareTo(new BigDecimal("264400")) < 0,
                "deuda calculada: " + deuda);
    }

    @Test
    void filtroPorEstadoConsultaSoloEseEstado() {
        useCase.execute(filtro().estado(EstadoSolicitud.REVISION_MANUAL).build());
        assertEquals(Set.of(EstadoSolicitud.REVISION_MANUAL), estadosConsultados);
    }

    @Test
    void noPermiteListarAprobadas() {
        FiltroSolicitudes f = filtro().estado(EstadoSolicitud.APROBADA).build();
        assertThrows(ValidacionException.class, () -> useCase.execute(f));
    }

    @Test
    void paginacionInvalida() {
        assertThrows(ValidacionException.class, () -> useCase.execute(filtro().pagina(-1).build()));
        assertThrows(ValidacionException.class, () -> useCase.execute(filtro().tamano(0).build()));
        assertThrows(ValidacionException.class, () -> useCase.execute(filtro().tamano(51).build()));
    }

    @Test
    void solicitudSinEmailNoRompeElListado() {
        solicitudes.clear();
        solicitudes.add(Solicitud.builder().id(9L).monto(BigDecimal.TEN).plazoMeses(1).idTipoPrestamo(1L)
                .estado(EstadoSolicitud.PENDIENTE_REVISION).build());
        SolicitudRevision fila = useCase.execute(filtro().build()).contenido().get(0);
        assertNull(fila.nombre());
        assertEquals(new BigDecimal("0.00"), fila.deudaTotalMensualSolicitudesAprobadas());
    }

    @Test
    void cuotaSinInteresEsMontoEntrePlazo() {
        assertEquals(0, ListarSolicitudesRevisionUseCase
                .cuotaMensual(new BigDecimal("1200"), BigDecimal.ZERO, 12).compareTo(new BigDecimal("100")));
    }
}
