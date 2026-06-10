package com.ceqb.SistemaVentaDispositivos2025.model;

public enum EstadoPedido {

    CREADO("Creado", "default"),            // ⚪ Nuevo estado
    PROCESANDO("Procesando", "warning"),
    CONFIRMADO("Confirmado","success"),// 🟢 Verde
    EN_CAMINO("En camino", "info"),        // 🔵 Azul
    ENTREGADO("Entregado", "success"),     // 🟢 Verde
    CANCELADO("Cancelado", "danger"),
    ABANDONADO("Abandonado", "danger"),// 🔴 Rojo
    ENTREGADO_REVISADO("Entregado Revisado", "secondary"); // 🟢 Gris
    //ENTREGADO_NOTIFICACION_PENDIENTE("Notificacion Entregado", "success");


    public boolean esFinal() {
        return switch (this) {
            case ENTREGADO,
                 ENTREGADO_REVISADO,
                 CANCELADO,
                 ABANDONADO -> true;
            default -> false;
        };
    }

    //ASIGNADO("Asignado", "success");
    private final String label;
    private final String bootstrapColor;

    EstadoPedido(String label, String bootstrapColor) {
        this.label = label;
        this.bootstrapColor = bootstrapColor;
    }

    public String getLabel() {
        return label;
    }

    public String getBootstrapColor() {
        return bootstrapColor;
    }
}
