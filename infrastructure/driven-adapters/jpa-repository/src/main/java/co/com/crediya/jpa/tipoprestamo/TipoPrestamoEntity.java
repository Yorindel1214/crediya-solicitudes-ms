package co.com.crediya.jpa.tipoprestamo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Cómo se guarda un tipo de préstamo en la tabla "tipos_prestamo" (cargada desde db/init.sql).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tipos_prestamo")
public class TipoPrestamoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    /** HU4: tasa nominal anual en porcentaje. */
    @Column(name = "tasa_interes", precision = 5, scale = 2)
    private BigDecimal tasaInteres;
}
