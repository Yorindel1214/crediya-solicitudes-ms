package co.com.crediya.jpa.solicitud;

import co.com.crediya.model.comun.Pagina;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroSolicitudes;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    @Override
    @Transactional(readOnly = true)
    public Pagina<Solicitud> buscarPagina(FiltroSolicitudes filtro, Set<EstadoSolicitud> estados) {
        log.info("Buscando solicitudes: estados={}, email={}, tipo={}, página={}, tamaño={}",
                estados, filtro.email(), filtro.idTipoPrestamo(), filtro.pagina(), filtro.tamano());

        // Siempre filtra por estado; email y tipo de préstamo solo si vienen
        Specification<SolicitudEntity> consulta = (raiz, q, cb) ->
                raiz.get("estado").in(estados.stream().map(Enum::name).toList());
        if (filtro.email() != null && !filtro.email().isBlank()) {
            String email = filtro.email().trim().toLowerCase(Locale.ROOT);
            consulta = consulta.and((raiz, q, cb) -> cb.equal(cb.lower(raiz.get("email")), email));
        }
        if (filtro.idTipoPrestamo() != null) {
            consulta = consulta.and((raiz, q, cb) -> cb.equal(raiz.get("idTipoPrestamo"), filtro.idTipoPrestamo()));
        }

        PageRequest pedido = PageRequest.of(filtro.pagina(), filtro.tamano(),
                Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by("id")));
        Page<SolicitudEntity> pagina = repository.findAll(consulta, pedido);
        log.info("Se encontraron {} solicitudes en total", pagina.getTotalElements());
        return new Pagina<>(pagina.getContent().stream().map(this::aDominio).toList(),
                filtro.pagina(), filtro.tamano(), pagina.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarPorEmailsYEstado(Collection<String> emails, EstadoSolicitud estado) {
        return repository.findByEmailInAndEstado(emails, estado.name()).stream().map(this::aDominio).toList();
    }

    private SolicitudEntity aEntidad(Solicitud s) {
        return new SolicitudEntity(
                s.getId(),
                s.getDocumentoIdentidad(),
                s.getMonto(),
                s.getPlazoMeses(),
                s.getIdTipoPrestamo(),
                s.getEstado().name(),
                s.getFechaCreacion(),
                s.getEmail());
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
                .email(e.getEmail())
                .build();
    }
}
