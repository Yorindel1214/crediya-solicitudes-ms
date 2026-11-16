package co.com.crediya.model.tipoprestamo.gateways;

import co.com.crediya.model.tipoprestamo.TipoPrestamo;

import java.util.Optional;

/**
 * Contrato (puerto de salida): "alguien que sepa buscar tipos de préstamo".
 * Optional = "puede que lo encuentre o puede que no".
 */
public interface TipoPrestamoRepository {
    Optional<TipoPrestamo> buscarPorId(Long id);
}
