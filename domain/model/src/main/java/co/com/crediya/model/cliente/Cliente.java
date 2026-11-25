package co.com.crediya.model.cliente;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * HU4: datos del cliente que guarda el microservicio de Autenticación y que el asesor necesita ver.
 */
@Builder
public record Cliente(String email, String nombres, String apellidos, String documentoIdentidad,
                      BigDecimal salarioBase) {

    public String nombreCompleto() {
        return ((nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos)).trim();
    }
}
