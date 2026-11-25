package co.com.crediya.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Una página de resultados")
public record PaginaResponse<T>(
        @Schema(description = "Elementos de esta página")
        List<T> contenido,
        @Schema(description = "Número de página (empieza en 0)", example = "0")
        int pagina,
        @Schema(description = "Elementos por página", example = "10")
        int tamano,
        @Schema(description = "Total de elementos que cumplen los filtros", example = "23")
        @JsonProperty("total_elementos") long totalElementos,
        @Schema(description = "Total de páginas", example = "3")
        @JsonProperty("total_paginas") int totalPaginas) {
}
