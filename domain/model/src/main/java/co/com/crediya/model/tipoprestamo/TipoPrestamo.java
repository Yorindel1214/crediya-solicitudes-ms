package co.com.crediya.model.tipoprestamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    /** HU4: tasa de interés nominal anual en porcentaje (ej. 24.00 = 24 % anual). */
    private BigDecimal tasaInteres;
}
