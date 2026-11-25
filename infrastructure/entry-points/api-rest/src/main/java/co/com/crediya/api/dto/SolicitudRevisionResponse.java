package co.com.crediya.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Solicitud pendiente de la decisión del asesor")
public record SolicitudRevisionResponse(
        @Schema(description = "Número de la solicitud", example = "5")
        Long id,
        @Schema(description = "Monto solicitado", example = "5000000")
        BigDecimal monto,
        @Schema(description = "Plazo en meses", example = "24")
        Integer plazo,
        @Schema(description = "Correo del cliente", example = "carlos@correo.com")
        String email,
        @Schema(description = "Nombre completo del cliente", example = "Carlos Ruiz")
        String nombre,
        @Schema(description = "Tipo de préstamo", example = "Libre inversión")
        @JsonProperty("tipo_prestamo") String tipoPrestamo,
        @Schema(description = "Tasa de interés nominal anual (%)", example = "24.00")
        @JsonProperty("tasa_interes") BigDecimal tasaInteres,
        @Schema(description = "Estado de la solicitud", example = "Pendiente de revisión")
        @JsonProperty("estado_solicitud") String estadoSolicitud,
        @Schema(description = "Salario base del cliente", example = "5000000")
        @JsonProperty("salario_base") BigDecimal salarioBase,
        @Schema(description = "Suma de las cuotas mensuales de las solicitudes APROBADAS del cliente", example = "264358.12")
        @JsonProperty("deuda_total_mensual_solicitudes_aprobadas") BigDecimal deudaTotalMensualSolicitudesAprobadas) {
}
