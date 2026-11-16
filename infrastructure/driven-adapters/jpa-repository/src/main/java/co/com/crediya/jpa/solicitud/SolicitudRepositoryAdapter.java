package co.com.crediya.jpa.solicitud;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cumple el contrato SolicitudRepository del dominio usando PostgreSQL (JPA).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SolicitudRepositoryAdapter implements SolicitudRepository {

    private final SolicitudJpaRepository repository;

    @Override
    @Transactional
    public Solicitud guardar(Solicitud solicitud) {
        log.info("Guardando solicitud del documento {} por {} a {} meses",
                solicitud.getDocumentoIdentidad(), solicitud.getMonto(), solicitud.getPlazoMeses());
        SolicitudEntity guardada = repository.saveAndFlush(aEntidad(solicitud));
        log.info("Solicitud guardada con id {} y estado {}", guardada.getId(), guardada.getEstado());
        return aDominio(guardada);
    }

    private SolicitudEntity aEntidad(Solicitud s) {
        return new SolicitudEntity(
                s.getId(),
                s.getDocumentoIdentidad(),
                s.getMonto(),
                s.getPlazoMeses(),
                s.getIdTipoPrestamo(),
                s.getEstado().name(),
                s.getFechaCreacion());
    }

    private Solicitud aDominio(SolicitudEntity e) {
        return Solicitud.builder()
                .id(e.getId())
                .documentoIdentidad(e.getDocumentoIdentidad())
                .monto(e.getMonto())
                .plazoMeses(e.getPlazoMeses())
                .idTipoPrestamo(e.getIdTipoPrestamo())
                .estado(EstadoSolicitud.valueOf(e.getEstado()))
                .fechaCreacion(e.getFechaCreacion())
                .build();
    }
}
