package co.com.crediya.usecase.registrarsolicitud;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exceptions.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exceptions.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.exceptions.ValidacionException;
import co.com.crediya.model.tipoprestamo.TipoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarSolicitudUseCaseTest {

    private static final String DOCUMENTO = "1023456789";
    private static final String CORREO = "carlos@correo.com";

    private final List<Solicitud> guardadas = new ArrayList<>();
    private RegistrarSolicitudUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarSolicitudUseCase(
                solicitud -> {
                    Solicitud conId = solicitud.toBuilder().id((long) guardadas.size() + 1).build();
                    guardadas.add(conId);
                    return conId;
                },
                id -> id == 1L
                        ? Optional.of(TipoPrestamo.builder().id(1L).nombre("Libre inversión").build())
                        : Optional.empty());
    }

    private Solicitud.SolicitudBuilder valida() {
        return Solicitud.builder()
                .documentoIdentidad(DOCUMENTO)
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .idTipoPrestamo(1L);
    }

    @Test
    void clienteCreaSolicitudASuNombre() {
        Solicitud guardada = useCase.execute(valida().build(), DOCUMENTO, CORREO);

        assertEquals(EstadoSolicitud.PENDIENTE_REVISION, guardada.getEstado());
        assertEquals(CORREO, guardada.getEmail());
        assertEquals("Libre inversión", guardada.getNombreTipoPrestamo());
        assertEquals(1, guardadas.size());
    }

    @Test
    void clienteNoPuedeCrearSolicitudAOtroDocumento() {
        Solicitud ajena = valida().documentoIdentidad("99999999").build();
        assertThrows(AccesoDenegadoException.class, () -> useCase.execute(ajena, DOCUMENTO, CORREO));
        assertTrue(guardadas.isEmpty());
    }

    @Test
    void tipoDePrestamoInexistente() {
        Solicitud solicitud = valida().idTipoPrestamo(77L).build();
        assertThrows(TipoPrestamoNoExisteException.class, () -> useCase.execute(solicitud, DOCUMENTO, CORREO));
    }

    @Test
    void montoCeroEsInvalido() {
        Solicitud solicitud = valida().monto(BigDecimal.ZERO).build();
        assertThrows(ValidacionException.class, () -> useCase.execute(solicitud, DOCUMENTO, CORREO));
    }

    @Test
    void plazoFueraDeRangoEsInvalido() {
        Solicitud solicitud = valida().plazoMeses(400).build();
        assertThrows(ValidacionException.class, () -> useCase.execute(solicitud, DOCUMENTO, CORREO));
    }
}
