package co.com.crediya.model.tipoprestamo.gateways;

import co.com.crediya.model.tipoprestamo.TipoPrestamo;

import java.util.List;
import java.util.Optional;

/**
 * Contrato (puerto de salida): "alguien que sepa buscar tipos de préstamo".
 * Optional = "puede que lo encuentre o puede que no".
 */
public interface TipoPrestamoRepository {
    Optional<TipoPrestamo> buscarPorId(Long id);
    // HU4: el catálogo completo (son pocos) para mostrar nombre y tasa en el listado
    List<TipoPrestamo> buscarTodos();
}
