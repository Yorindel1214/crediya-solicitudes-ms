package co.com.crediya.jpa.tipoprestamo;

import co.com.crediya.model.tipoprestamo.TipoPrestamo;
import co.com.crediya.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cumple el contrato TipoPrestamoRepository del dominio consultando la tabla "tipos_prestamo".
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TipoPrestamoRepositoryAdapter implements TipoPrestamoRepository {

    private final TipoPrestamoJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoPrestamo> buscarPorId(Long id) {
        log.info("Buscando tipo de préstamo con id {}", id);
        return repository.findById(id)
                .map(e -> TipoPrestamo.builder()
                        .id(e.getId())
                        .nombre(e.getNombre())
                        .descripcion(e.getDescripcion())
                        .build());
    }
}
