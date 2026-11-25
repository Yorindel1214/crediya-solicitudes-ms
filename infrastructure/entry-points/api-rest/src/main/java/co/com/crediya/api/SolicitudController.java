package co.com.crediya.api;

import co.com.crediya.api.dto.ErrorResponse;
import co.com.crediya.api.dto.SolicitudRequest;
import co.com.crediya.api.dto.SolicitudResponse;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.registrarsolicitud.RegistrarSolicitudUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/solicitud")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Solicitudes de préstamo")
public class SolicitudController {

    private final RegistrarSolicitudUseCase registrarSolicitudUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar una solicitud de préstamo (CLIENTE)",
            description = "Requiere el token de un CLIENTE. El cliente envía su documento, el monto, el plazo y el "
                    + "tipo de préstamo; el documento debe ser el mismo de su sesión (solo puede pedir préstamos "
                    + "a su nombre). Se valida la información, que el tipo de préstamo exista, y la solicitud queda "
                    + "registrada con estado \"Pendiente de revisión\".")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud registrada",
                    content = @Content(schema = @Schema(implementation = SolicitudResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de préstamo inexistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No envió token o el token no es válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es CLIENTE o el documento no es el suyo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public SolicitudResponse registrar(@RequestBody SolicitudRequest request, @AuthenticationPrincipal Jwt token) {
        log.info("POST /api/v1/solicitud recibido de {}", token.getSubject());
        Solicitud solicitud = Solicitud.builder()
                .documentoIdentidad(request.documentoIdentidad())
                .monto(request.monto())
                .plazoMeses(request.plazoMeses())
                .idTipoPrestamo(request.idTipoPrestamo())
                .build();

        Solicitud guardada = registrarSolicitudUseCase.execute(
                solicitud, token.getClaimAsString("documento"), token.getSubject());

        return new SolicitudResponse(
                guardada.getId(),
                guardada.getDocumentoIdentidad(),
                guardada.getEmail(),
                guardada.getMonto(),
                guardada.getPlazoMeses(),
                guardada.getIdTipoPrestamo(),
                guardada.getNombreTipoPrestamo(),
                guardada.getEstado().getDescripcion(),
                guardada.getFechaCreacion());
    }
}
