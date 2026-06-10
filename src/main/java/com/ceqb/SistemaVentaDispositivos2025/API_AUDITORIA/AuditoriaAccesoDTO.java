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
public class AuditoriaAccesoDTO implements Serializable {
    private String usuario;
    private String ip;
    private String navegador;
    private LocalDateTime fechaHora;
    private boolean exitoso;
    private String observacion;
}
