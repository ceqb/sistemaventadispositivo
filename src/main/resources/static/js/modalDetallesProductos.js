document.addEventListener('DOMContentLoaded', function () {
    const ejemploModal = document.getElementById('ejemploModal');

    if (ejemploModal) {
        ejemploModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;

            // Obtener datos del producto
            const productoId = button.getAttribute('data-id'); // 👈 ID del producto
            const nombre = button.getAttribute('data-nombre');
            const descripcion = button.getAttribute('data-descripcion');
            const precio = button.getAttribute('data-precio');
            const videoUrl = button.getAttribute('data-video');
            const imagenUrl = button.getAttribute('data-imagen');

            // Referencias del modal
            const modalNombre = ejemploModal.querySelector('#modalNombre');
            const modalDescripcion = ejemploModal.querySelector('#modalDescripcion');
            const modalPrecio = ejemploModal.querySelector('#modalPrecio');
            const mediaContainer = ejemploModal.querySelector('#modal-media-container');

            // Actualizar contenido del modal
            if (modalNombre) modalNombre.innerText = nombre;
            if (modalDescripcion) modalDescripcion.innerText = descripcion;
            if (modalPrecio) modalPrecio.innerText = `S/ ${precio}`;

            if (mediaContainer) {
                mediaContainer.innerHTML = '';

                // Mostrar video si existe, sino imagen
                if (videoUrl && videoUrl.trim() !== '' && videoUrl !== 'null') {
                    const videoHtml = `<video controls autoplay class="img-fluid mb-3 rounded">
                                        <source src="${videoUrl}" type="video/mp4">
                                        Tu navegador no soporta el video.
                                       </video>`;
                    mediaContainer.innerHTML = videoHtml;
                } else if (imagenUrl) {
                    const imagenHtml = `<img src="${imagenUrl}" class="img-fluid mb-3 rounded" alt="${nombre}">`;
                    mediaContainer.innerHTML = imagenHtml;
                } else {
                    mediaContainer.innerHTML = '<p>No hay contenido multimedia disponible.</p>';
                }
            }
        });

        // Detener el video al cerrar el modal
        ejemploModal.addEventListener('hide.bs.modal', function () {
            const video = ejemploModal.querySelector('video');
            if (video) {
                video.pause();
                video.currentTime = 0;
            }
        });
    } else {
        console.error("DEBUG JS: Modal #ejemploModal no encontrado.");
    }
});
