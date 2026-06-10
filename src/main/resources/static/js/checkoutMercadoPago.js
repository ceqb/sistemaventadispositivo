 document.getElementById('checkoutForm').addEventListener('submit', function(e) {
        e.preventDefault();

        console.log("Iniciando llamada fetch para crear preferencia de pago.");

        fetch('/pedidos/create-payment-preference', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        .then(response => {
            console.log("Respuesta recibida del servidor. Estado:", response.status);

            if (response.status === 401) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Debes iniciar sesión',
                    text: 'Para completar la compra necesitas iniciar sesión.',
                    confirmButtonText: 'Aceptar'
                }).then(() => {
                    // Redirigir a la URL de login
                    // Mostrar modal en lugar de redirigir
                   showLoginModal();
                });
                return null;
            }

            if (!response.ok) {
                return response.text().then(text => {
                    try {
                        const data = JSON.parse(text);
                        throw new Error(data.error || 'Error desconocido');
                    } catch (e) {
                        console.error("Respuesta no JSON:", text);
                        throw new Error('Error inesperado');
                    }
                });
            }

            return response.json();
        })
        .then(data => {
            if (!data) return;

            console.log("Datos recibidos:", data);

            if (data.url_pago) {
                console.log("URL de pago recibida. Redirigiendo a:", data.url_pago);
                window.location.href = data.url_pago;
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se recibió la URL de pago.'
                });
            }
        })
        .catch(error => {
            console.error("Error en fetch:", error);
            Swal.fire({
                icon: 'error',
                title: 'Error al procesar el pago',
                text: error.message
            });
        });
    });