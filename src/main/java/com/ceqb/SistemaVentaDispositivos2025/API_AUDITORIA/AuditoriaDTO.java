package com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaDTO implements Serializable {

    private Long idAuditoria;
    private String entidad;
    private String accion;
    private String descripcion;
    private LocalDateTime fechaHora;

}
