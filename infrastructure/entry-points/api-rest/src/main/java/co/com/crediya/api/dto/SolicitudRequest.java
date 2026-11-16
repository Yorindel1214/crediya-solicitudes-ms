package co.com.crediya.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos para enviar una solicitud de préstamo")
public record SolicitudRequest(
        @Schema(description = "Documento de identidad del cliente (solo números, 6 a 15 dígitos)", example = "1023456789")
        @JsonProperty("documento_identidad") String documentoIdentidad,
        @Schema(description = "Monto solicitado, mayor que 0", example = "5000000")
        BigDecimal monto,
        @Schema(description = "Plazo deseado en meses, entre 1 y 360", example = "24")
        @JsonProperty("plazo_meses") Integer plazoMeses,
        @Schema(description = "Id del tipo de préstamo (1 Libre inversión, 2 Vehículo, 3 Vivienda, 4 Educación)", example = "1")
        @JsonProperty("id_tipo_prestamo") Long idTipoPrestamo) {
}
