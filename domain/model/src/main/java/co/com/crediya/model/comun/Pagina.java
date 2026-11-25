package co.com.crediya.model.comun;

import java.util.List;
import java.util.function.Function;

/**
 * Una página de resultados: los elementos de la página y cuántos hay en total.
 * Es del dominio (no depende de Spring), por eso no se usa Page de Spring Data aquí.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos) {

    public int totalPaginas() {
        return tamano == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
    }

    public <R> Pagina<R> mapear(Function<T, R> funcion) {
        return new Pagina<>(contenido.stream().map(funcion).toList(), pagina, tamano, totalElementos);
    }
}
