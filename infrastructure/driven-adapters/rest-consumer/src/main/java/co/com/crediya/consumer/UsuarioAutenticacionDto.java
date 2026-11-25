package co.com.crediya.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Lo que responde GET /api/v1/usuarios del microservicio de Autenticación (solo los campos que usamos).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioAutenticacionDto(
        String nombres,
        String apellidos,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("documento_identidad") String documentoIdentidad,
        @JsonProperty("salario_base") BigDecimal salarioBase) {
}
