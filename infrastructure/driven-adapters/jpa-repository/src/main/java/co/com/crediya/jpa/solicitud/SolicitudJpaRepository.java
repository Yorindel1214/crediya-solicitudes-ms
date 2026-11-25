package co.com.crediya.jpa.solicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

/**
 * JpaSpecificationExecutor permite armar la consulta del listado (HU4) con filtros opcionales.
 */
public interface SolicitudJpaRepository extends JpaRepository<SolicitudEntity, Long>,
        JpaSpecificationExecutor<SolicitudEntity> {

    List<SolicitudEntity> findByEmailInAndEstado(Collection<String> emails, String estado);
}
