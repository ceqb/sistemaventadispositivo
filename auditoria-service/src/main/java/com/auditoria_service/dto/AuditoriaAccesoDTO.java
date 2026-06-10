package com.auditoria_service.dto;

import com.auditoria_service.model.Auditoria;
import com.auditoria_service.model.AuditoriaAcceso;
import lombok.*;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaAccesoDTO implements Serializable {

    private Long idAuditoriaAcceso;
    private String usuario;
    private String ip;
    private String navegador;
    private LocalDateTime fechaHora;
    private boolean exitoso;
    private String observacion;

    public static AuditoriaAccesoDTO toDto(AuditoriaAcceso model) {
        if (model == null) return null;

        return AuditoriaAccesoDTO.builder()
                .idAuditoriaAcceso(model.getIdAuditoriaAcceso())
                .usuario(model.getUsuario())
                .ip(model.getIp())
                .navegador(model.getNavegador())
                .fechaHora(model.getFechaHora())
                .exitoso(model.isExitoso())
                .observacion(model.getObservacion())

                .build();
    }
    public static List<AuditoriaAccesoDTO> toDto(List<AuditoriaAcceso> models) {
        if (models == null) return Collections.emptyList();
        return models.stream()
                .map(AuditoriaAccesoDTO::toDto).collect(Collectors.toList());
    }
}
