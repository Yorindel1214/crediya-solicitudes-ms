package co.com.crediya.model.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una solicitud de préstamo enviada por un cliente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private Long id;
    private String documentoIdentidad;
    private BigDecimal monto;
    private Integer plazoMeses;
    private Long idTipoPrestamo;
    private String nombreTipoPrestamo;
    private EstadoSolicitud estado;
    private LocalDateTime fechaCreacion;
    // HU3: correo del cliente que inició sesión y creó la solicitud (sale del token)
    private String email;
}
