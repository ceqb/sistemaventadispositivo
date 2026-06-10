function verProductoEnWhatsApp(elemento) {
        try {
            // Lee los datos de los atributos 'data-'
            const nombreProducto = elemento.getAttribute('data-nombre');
            const precioProducto = elemento.getAttribute('data-precio');

            const mensaje = `Hola, estoy interesado en el producto: ${nombreProducto} de: S/. ${precioProducto}. ¿Podrías darme más detalles?`;
            const telefono = '51912751437'; // Cambia esto a tu número
            const url = `https://wa.me/${telefono}?text=${encodeURIComponent(mensaje)}`;

            window.open(url, '_blank');
        } catch (error) {
            console.error('Hubo un problema al intentar enviar el mensaje:', error);
        }
    }
