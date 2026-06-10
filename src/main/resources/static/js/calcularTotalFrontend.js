 function calcularTotal() {
        const cantidad = parseFloat(document.getElementById("cantidad").value) || 0;
        const precioUnitario = parseFloat(document.getElementById("precioUnitario").value) || 0;
        const total = cantidad * precioUnitario;
        document.getElementById("total").value = total.toFixed(2);
    }

    document.getElementById("cantidad").addEventListener("input", calcularTotal);
    document.getElementById("precioUnitario").addEventListener("input", calcularTotal);