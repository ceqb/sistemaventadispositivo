package com.ceqb.SistemaVentaDispositivos2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClicDTO implements Serializable {
    private Long productoId;
    private String userId;
}
