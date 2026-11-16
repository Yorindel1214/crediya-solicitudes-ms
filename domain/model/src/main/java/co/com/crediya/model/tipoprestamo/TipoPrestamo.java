package co.com.crediya.model.tipoprestamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un tipo de préstamo del catálogo de CrediYa (Libre inversión, Vehículo, Vivienda...).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TipoPrestamo {
    private Long id;
    private String nombre;
    private String descripcion;
}
