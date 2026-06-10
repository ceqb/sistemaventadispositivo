package com.ceqb.SistemaVentaDispositivos2025.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class PedidoEntregadoEvent {
    private final Long pedidoId;
    private final BigDecimal total;
}
