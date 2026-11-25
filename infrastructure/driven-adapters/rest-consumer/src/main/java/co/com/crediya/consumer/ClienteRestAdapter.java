package co.com.crediya.consumer;

import co.com.crediya.model.cliente.Cliente;
import co.com.crediya.model.cliente.gateways.ClienteGateway;
import co.com.crediya.model.solicitud.exceptions.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.List;

/**
 * Cumple el contrato ClienteGateway llamando por HTTP al microservicio de Autenticación:
 * GET {autenticacion.url}/api/v1/usuarios?correos=a@x.com,b@y.com
 * <p>
 * Reenvía el mismo token del asesor que hizo la petición (Authorization: Bearer ...),
 * así el microservicio de Autenticación también valida que tenga permiso.
 */
@Slf4j
@Component
public class ClienteRestAdapter implements ClienteGateway {

    private static final ParameterizedTypeReference<List<UsuarioAutenticacionDto>> LISTA_USUARIOS =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public ClienteRestAdapter(@Value("${autenticacion.url}") String urlAutenticacion) {
        this.restClient = RestClient.builder().baseUrl(urlAutenticacion).build();
    }

    @Override
    public List<Cliente> buscarPorEmails(Collection<String> emails) {
        log.info("Consultando {} clientes en el microservicio de Autenticación", emails.size());
        try {
            List<UsuarioAutenticacionDto> usuarios = restClient.get()
                    .uri(uri -> uri.path("/api/v1/usuarios").queryParam("correos", String.join(",", emails)).build())
                    .headers(cabeceras -> {
                        String token = tokenDeLaPeticionActual();
                        if (token != null) {
                            cabeceras.set(HttpHeaders.AUTHORIZATION, token);
                        }
                    })
                    .retrieve()
                    .body(LISTA_USUARIOS);
            log.info("El microservicio de Autenticación devolvió {} clientes", usuarios == null ? 0 : usuarios.size());
            return usuarios == null ? List.of() : usuarios.stream().map(ClienteRestAdapter::aDominio).toList();
        } catch (RestClientException e) {
            log.error("Error consultando el microservicio de Autenticación: {}", e.getMessage());
            throw new ServicioNoDisponibleException(
                    "No fue posible consultar los datos de los clientes. Intente nuevamente más tarde");
        }
    }

    private static Cliente aDominio(UsuarioAutenticacionDto u) {
        return Cliente.builder()
                .email(u.correoElectronico())
                .nombres(u.nombres())
                .apellidos(u.apellidos())
                .documentoIdentidad(u.documentoIdentidad())
                .salarioBase(u.salarioBase())
                .build();
    }

    private static String tokenDeLaPeticionActual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            return atributos.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
