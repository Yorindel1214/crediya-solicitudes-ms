package co.com.crediya.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Solicitud de préstamo registrada")
public record SolicitudResponse(
        @Schema(description = "Identificador generado por el sistema", example = "1")
        Long id,
        @Schema(description = "Documento de identidad del cliente", example = "1023456789")
        @JsonProperty("documento_identidad") String documentoIdentidad,
        @Schema(description = "Monto solicitado", example = "5000000")
        BigDecimal monto,
        @Schema(description = "Plazo en meses", example = "24")
        @JsonProperty("plazo_meses") Integer plazoMeses,
        @Schema(description = "Id del tipo de préstamo", example = "1")
        @JsonProperty("id_tipo_prestamo") Long idTipoPrestamo,
        @Schema(description = "Nombre del tipo de préstamo", example = "Libre inversión")
        @JsonProperty("tipo_prestamo") String tipoPrestamo,
        @Schema(description = "Estado de la solicitud", example = "Pendiente de revisión")
        String estado,
        @Schema(description = "Fecha y hora en que se registró", example = "2026-11-16T10:30:00")
        @JsonProperty("fecha_creacion") LocalDateTime fechaCreacion) {
}
