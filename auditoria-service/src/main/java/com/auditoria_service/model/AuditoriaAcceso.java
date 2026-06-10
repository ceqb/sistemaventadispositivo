package com.auditoria_service.model;

import com.auditoria_service.dto.AuditoriaAccesoDTO;
import com.auditoria_service.dto.AuditoriaDTO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auditoria_acceso")
public class AuditoriaAcceso implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria_acceso", nullable = false)
    private Long idAuditoriaAcceso;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Column(name = "ip")
    private String ip;

    @Column(name = "navegador")
    private String navegador;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "exitoso")
    private boolean exitoso;

    @Column(name = "observacion", length = 500)
    private String observacion;

    public static AuditoriaAcceso toModel(AuditoriaAccesoDTO dto) {
        if (dto == null) return null;

        return AuditoriaAcceso.builder()
                .idAuditoriaAcceso(dto.getIdAuditoriaAcceso())
                .usuario(dto.getUsuario())
                .ip(dto.getIp())
                .navegador(dto.getNavegador())
                .fechaHora(dto.getFechaHora())
                .exitoso(dto.isExitoso())
                .observacion(dto.getObservacion())
                .build();
    }
    public static List<AuditoriaAcceso> toModel(List<AuditoriaAccesoDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(AuditoriaAcceso::toModel).collect(Collectors.toList());
    }
}
