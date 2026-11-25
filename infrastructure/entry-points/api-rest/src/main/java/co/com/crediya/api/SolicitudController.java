package co.com.crediya.api;

import co.com.crediya.api.dto.ErrorResponse;
import co.com.crediya.api.dto.PaginaResponse;
import co.com.crediya.api.dto.SolicitudRequest;
import co.com.crediya.api.dto.SolicitudResponse;
import co.com.crediya.api.dto.SolicitudRevisionResponse;
import co.com.crediya.model.comun.Pagina;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroSolicitudes;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.exceptions.ValidacionException;
import co.com.crediya.usecase.listarsolicitudes.ListarSolicitudesRevisionUseCase;
import co.com.crediya.usecase.registrarsolicitud.RegistrarSolicitudUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/v1/solicitud")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Solicitudes de préstamo")
public class SolicitudController {

    private final RegistrarSolicitudUseCase registrarSolicitudUseCase;
    private final ListarSolicitudesRevisionUseCase listarSolicitudesRevisionUseCase;

    @GetMapping
    @Operation(
            summary = "Listar solicitudes para revisión manual (ASESOR)",
            description = "HU4: lista paginada de las solicitudes que esperan la decisión del asesor "
                    + "(Pendiente de revisión, Rechazada y Revisión manual). Se puede filtrar por estado, email del "
                    + "cliente y tipo de préstamo. Incluye nombre y salario base del cliente (consultados al "
                    + "microservicio de Autenticación) y la deuda mensual por sus solicitudes aprobadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de solicitudes"),
            @ApiResponse(responseCode = "400", description = "Filtros o paginación inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No envió token o el token no es válido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no es ASESOR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "El microservicio de Autenticación no respondió",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PaginaResponse<SolicitudRevisionResponse> listarParaRevision(
            @Parameter(description = "PENDIENTE_REVISION, RECHAZADA o REVISION_MANUAL (vacío = los tres)")
            @RequestParam(name = "estado", required = false) String estado,
            @Parameter(description = "Correo del cliente") @RequestParam(name = "email", required = false) String email,
            @Parameter(description = "Id del tipo de préstamo")
            @RequestParam(name = "id_tipo_prestamo", required = false) Long idTipoPrestamo,
            @Parameter(description = "Número de página, empieza en 0")
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @Parameter(description = "Elementos por página, de 1 a 50")
            @RequestParam(name = "tamano", defaultValue = "10") int tamano,
            @AuthenticationPrincipal Jwt token) {
        log.info("GET /api/v1/solicitud recibido de {}: estado={}, email={}, tipo={}, página={}, tamaño={}",
                token.getSubject(), estado, email, idTipoPrestamo, pagina, tamano);
        FiltroSolicitudes filtro = FiltroSolicitudes.builder()
                .estado(estadoDelTexto(estado))
                .email(email)
                .idTipoPrestamo(idTipoPrestamo)
                .pagina(pagina)
                .tamano(tamano)
                .build();

        Pagina<SolicitudRevision> resultado = listarSolicitudesRevisionUseCase.execute(filtro);
        log.info("Listado para revisión: {} de {} solicitudes", resultado.contenido().size(), resultado.totalElementos());
        return new PaginaResponse<>(
                resultado.contenido().stream().map(SolicitudController::aRespuesta).toList(),
                resultado.pagina(),
                resultado.tamano(),
                resultado.totalElementos(),
                resultado.totalPaginas());
    }

    private static SolicitudRevisionResponse aRespuesta(SolicitudRevision s) {
        return new SolicitudRevisionResponse(
                s.id(), s.monto(), s.plazo(), s.email(), s.nombre(), s.tipoPrestamo(), s.tasaInteres(),
                s.estadoSolicitud().getDescripcion(), s.salarioBase(), s.deudaTotalMensualSolicitudesAprobadas());
    }

    private static EstadoSolicitud estadoDelTexto(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoSolicitud.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("El estado debe ser PENDIENTE_REVISION, RECHAZADA o REVISION_MANUAL");
        }
    }

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
