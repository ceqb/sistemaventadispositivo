   let pedidosPendientesCount = window.pedidosPendientesCount || 0;
   let entregasCompletadasCount = window.entregasCompletadasCount || 0;

    // =========================================================
        // 🔥 EJECUTAR EL WS SOLO CUANDO EL DOM ESTÁ LISTO
        // =========================================================
        document.addEventListener("DOMContentLoaded", () => {

            console.log("DOM cargado, iniciando WebSocket…");

            // Conectar al endpoint WebSocket
            let socket = new SockJS('/ws-notificaciones');
            let stompClient = Stomp.over(socket);
            stompClient.debug = null; // Desactiva logs molestos

            stompClient.connect({}, function (frame) {

                console.log("WS conectado:", frame);

                // ================================
                // 📌 1. NUEVO PEDIDO → INCREMENTO
                // ================================
                stompClient.subscribe('/topic/notificaciones', function (msg) {
                    try {
                        const data = JSON.parse(msg.body);
                        console.log("Nuevo pedido recibido:", data);

                        incrementarPedidosPendientes();

                        mostrarToast(
                            "Nuevo Pedido Recibido",
                            `Pedido #${data.pedidoId} - Total: S/ ${data.total}`
                        );

                    } catch (e) {
                        console.error("Error procesando notificación:", e);
                    }
                });

                // ===========================================
                // 📌 2. PEDIDO ASIGNADO → DECREMENTO
                // ===========================================
                stompClient.subscribe('/topic/control-notificaciones', function (msg) {
                    const data = JSON.parse(msg.body);

                    if (data.action === "DECREMENTAR_PEDIDO") {
                        decrementarPedidosPendientes();
                    }
                });

                // ===========================================
                // 📌 3. PEDIDO ENTREGADO → TOAST + CONTADOR
                // ===========================================
                stompClient.subscribe('/topic/entregas-completadas', function (msg) {
                    try {
                        const data = JSON.parse(msg.body);

                        entregasCompletadasCount++;
                        actualizarContador("contadorEntregasCompletadas", entregasCompletadasCount);

                        mostrarToast(
                            "Pedido Entregado",
                            `Pedido #${data.pedidoId} entregado por ${data.repartidor || 'Repartidor Desconocido'}`
                        );

                    } catch (e) {
                        console.error("Error procesando entrega:", e);
                    }
                });

                // ===========================================
                // 📌 4. CONTROL → DECREMENTAR ENTREGAS
                // ===========================================
                stompClient.subscribe('/topic/control-entregas', function (msg) {
                    const data = JSON.parse(msg.body);
                    if (data.action === "DECREMENTAR_ENTREGAS") {
                        decrementarEntregasCompletadas();
                    }
                });

            }, function (error) {
                console.error("Error de conexión STOMP:", error);
            });

        });


        // =========================================================
        // 🔵 CONTADORES
        // =========================================================

        function actualizarContador(idBadge, count) {
            const badge = document.getElementById(idBadge);
            if (!badge) return;

            if (count > 0) {
                badge.innerText = count;
                badge.style.display = "inline-block";
                badge.style.backgroundColor = "#ef4444";
                badge.style.color = "white";
                badge.style.padding = "2px 6px";
                badge.style.borderRadius = "9999px";
                badge.style.fontSize = "0.75rem";
            } else {
                badge.style.display = "none";
            }
        }

        function incrementarPedidosPendientes() {
            pedidosPendientesCount++;
            actualizarContador("contadorNotificaciones", pedidosPendientesCount);
        }

        function decrementarPedidosPendientes() {
            if (pedidosPendientesCount > 0) {
                pedidosPendientesCount--;
                actualizarContador("contadorNotificaciones", pedidosPendientesCount);
            }
        }

        function decrementarEntregasCompletadas() {
            if (entregasCompletadasCount > 0) {
                entregasCompletadasCount--;
                actualizarContador("contadorEntregasCompletadas", entregasCompletadasCount);
            }
        }


        // =========================================================
        // 🔔 FUNCIÓN TOAST FLOTANTE
        // =========================================================
        function mostrarToast(titulo, mensaje) {

            const toast = document.createElement('div');
            toast.className =
                "fixed top-4 right-4 bg-green-600 text-white px-6 py-4 rounded-xl shadow-lg z-50";

            toast.innerHTML = `<strong>${titulo}</strong><br>${mensaje}`;

            document.body.appendChild(toast);

            setTimeout(() => {
                toast.style.opacity = "0";
            }, 3500);

            setTimeout(() => toast.remove(), 4500);
        }

    let socket = new SockJS('/ws-notificaciones');
        let stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
        console.log("✅ WebSocket conectado");

        stompClient.subscribe('/topic/pedidosRetrasados', function (message) {
            console.log("📩 Mensaje recibido:", message.body);

            const cantidad = parseInt(message.body);
            const badge = document.getElementById("contadorPedidosRetrasados");

            if (!badge) {
                console.error("❌ NO existe el elemento contadorPedidosRetrasados");
                return;
            }

            badge.innerText = cantidad;
            badge.style.display = "inline-block";
        });
    });