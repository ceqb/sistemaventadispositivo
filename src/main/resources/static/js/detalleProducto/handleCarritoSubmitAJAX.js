async function handleCarritoSubmit(event, productoId) {
    event.preventDefault();
    const form = event.target;
    const button = form.querySelector('button');

    // Feedback visual para el usuario
    if (button) {
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Procesando...';
    }

    form.submit();
    }