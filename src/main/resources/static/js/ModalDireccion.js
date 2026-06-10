document.addEventListener("DOMContentLoaded", () => {
    const btnPagar = document.getElementById("pagoContraEntregaForm");
    const modal = document.getElementById("modalDireccion");
    const direccionInput = document.getElementById("direccionModalInput");
    const error = document.getElementById("errorDireccionModal");
    const btnConfirmar = document.getElementById("btnConfirmarDireccionModal");
    const btnCancelar = document.getElementById("btnCancelarModal");

    if (!btnPagar || !modal) return;

    // Abrir modal
    btnPagar.addEventListener("click", (e) => {
        e.preventDefault();
        direccionInput.value = "";
        error.classList.add("hidden");
        modal.classList.remove("hidden");
        modal.querySelector("div").classList.remove("scale-95", "opacity-0");
        modal.querySelector("div").classList.add("scale-100", "opacity-100");
    });

    // Cerrar modal
    btnCancelar.addEventListener("click", () => {
        cerrarModal();
    });

    // Cerrar con click fuera
    modal.addEventListener("click", (e) => {
        if (e.target === modal) cerrarModal();
    });

    // Confirmar
    btnConfirmar.addEventListener("click", async () => {
        const direccion = direccionInput.value.trim();

        if (!direccion) {
            error.classList.remove("hidden");
            direccionInput.focus();
            return;
        }

        error.classList.add("hidden");
        btnConfirmar.disabled = true;
        btnConfirmar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';

        try {
            const response = await fetch("/pedidos/createPagoContraEntrega", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ direccion })
            });

            if (response.status === 401) {
                Swal.fire({
                    icon: "warning",
                    title: "Sesión expirada",
                    text: "Por favor inicia sesión nuevamente",
                    confirmButtonText: "Ir al login"
                }).then(() => window.location.href = "/login");
                return;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || "Error al procesar el pedido");
            }

            const data = await response.json();

            cerrarModal();

            // Mensaje de éxito premium
            Swal.fire({
                icon: "success",
                title: "¡Pedido Confirmado!",
                html: `<p class="text-lg mt-2">Tu pedido con <strong>Pago Contra Entrega</strong> ha sido registrado.</p>
                       <p class="text-sm text-gray-600 mt-4">Pronto recibirás detalles por WhatsApp.</p>`,
                confirmButtonText: "Ver mi pedido",
                confirmButtonColor: "#2563eb",
                showCancelButton: true,
                cancelButtonText: "Volver a la tienda",
                reverseButtons: true
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = data.redirectUrl || "/pedidosCliente/historialCliente";
                } else {
                    window.location.href = "/tienda";
                }
            });

        } catch (err) {
            console.error(err);
            Swal.fire({
                icon: "error",
                title: "Error",
                text: err.message || "No pudimos procesar tu pedido. Intenta nuevamente.",
                confirmButtonText: "Cerrar"
            });
        } finally {
            btnConfirmar.disabled = false;
            btnConfirmar.innerHTML = 'Confirmar Dirección <i class="fas fa-arrow-right ml-2"></i>';
        }
    });

    function cerrarModal() {
        modal.querySelector("div").classList.remove("scale-100", "opacity-100");
        modal.querySelector("div").classList.add("scale-95", "opacity-0");
        setTimeout(() => modal.classList.add("hidden"), 300);
    }
});