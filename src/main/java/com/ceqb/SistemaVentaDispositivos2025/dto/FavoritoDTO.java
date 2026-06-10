package com.ceqb.SistemaVentaDispositivos2025.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoritoDTO {

    private Long id;
    private Long usuarioId;
    private Long dispositivoId;

    private String productoModelo;
    private String rutaFoto;

    private LocalDateTime fechaRegistro;
}
