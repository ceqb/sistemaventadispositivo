package com.auditoria_service.model;


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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auditorias")
public class Auditoria implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(name = "entidad")
    private String entidad;

    @Column(name = "accion")
    private String accion;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fechaHora")
    private LocalDateTime fechaHora;

    public static Auditoria toModel(AuditoriaDTO dto) {
        if (dto == null) return null;

        return Auditoria.builder()
                .idAuditoria(dto.getIdAuditoria())
                .entidad(dto.getEntidad())
                .accion(dto.getAccion())
                .descripcion(dto.getDescripcion())
                .fechaHora(dto.getFechaHora())
                .build();
    }
    public static List<Auditoria> toModel(List<AuditoriaDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(Auditoria::toModel).collect(Collectors.toList());
    }
}
