package com.auditoria_service.dto;

import com.auditoria_service.model.Auditoria;
import jakarta.persistence.Column;
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
public class AuditoriaDTO implements Serializable {

    private Long idAuditoria;
    private String entidad;
    private String accion;
    private String descripcion;
    private LocalDateTime fechaHora;

    public static AuditoriaDTO toDto(Auditoria model) {
        if (model == null) return null;

        return AuditoriaDTO.builder()
                .idAuditoria(model.getIdAuditoria())
                .entidad(model.getEntidad())
                .accion(model.getAccion())
                .descripcion(model.getDescripcion())
                .fechaHora(model.getFechaHora())
                .build();
    }
    public static List<AuditoriaDTO> toDto(List<Auditoria> models) {
        if (models == null) return Collections.emptyList();
        return models.stream()
                .map(AuditoriaDTO::toDto).collect(Collectors.toList());
    }
}
