package co.com.crediya.model.cliente.gateways;

import co.com.crediya.model.cliente.Cliente;

import java.util.Collection;
import java.util.List;

/**
 * Puerto de salida: consultar clientes en el microservicio de Autenticación.
 * Al dominio no le importa que sea por HTTP; eso lo resuelve el adaptador.
 */
public interface ClienteGateway {
    List<Cliente> buscarPorEmails(Collection<String> emails);
}
