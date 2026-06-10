/* -----------------------------------------------------
   ADMIN: PREVISUALIZACIÓN EN EL FORMULARIO
----------------------------------------------------- */

// 1. Para la Foto Principal
window.previewPrincipal = function(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();

        reader.onload = function(e) {
            // --- 1. Lógica de Previsualización (La imagen pequeña) ---
            const preview = document.getElementById('imgPreview');
            const container = document.getElementById('containerPreviewPrincipal');
            if (preview) preview.src = e.target.result;
            if (container) container.classList.remove('hidden');

            // --- 2. Lógica de Efecto Verde (Igual que tu video) ---
            // Buscamos los elementos dentro del contenedor del input
            const parent = input.parentElement;
            const dropzone = parent.querySelector('div'); // El fondo azul
            const icon = parent.querySelector('i');       // El icono de nube
            const text = parent.querySelector('p');       // El texto

            if (icon) {
                // Cambia nube por aspa (check) y azul por verde
                icon.className = "fa fa-check-circle text-3xl text-emerald-500 mb-2 animate-bounce";
            }

            if (dropzone) {
                // Cambia el fondo y el borde a verde esmeralda
                dropzone.classList.remove('bg-blue-600/5', 'border-gray-200');
                dropzone.classList.add('bg-emerald-50', 'border-emerald-400');
            }

            if (text) {
                text.innerText = "¡FOTO SELECCIONADA!";
                text.classList.remove('text-gray-400');
                text.classList.add('text-emerald-600');
            }
        }

        reader.readAsDataURL(input.files[0]);
    }
};

// 2. Para la Galería (Múltiples fotos)
window.previewMultiple = function(input) {
    const container = document.getElementById('previewGaleria');
    const galeriaCompleta = document.getElementById('galeriaCompleta');
    const labelMas = document.getElementById('btn-add-galeria');
    const countLabel = document.getElementById('countGaleria');

    if (!container || !galeriaCompleta || !countLabel) return;

    // Contar fotos YA existentes (las que vienen de la BD y no fueron eliminadas)
    const fotosExistentes = galeriaCompleta.querySelectorAll('div.relative:not(#previewGaleria > *)').length;

    // Contar fotos nuevas que se van a previsualizar ahora
    const nuevasFotos = input.files.length;

    // Total preliminar
    let total = fotosExistentes + nuevasFotos;

    // Validar límite de 9
    if (total > 9) {
        alert("Máximo 9 imágenes permitidas en total (existentes + nuevas).");
        input.value = ""; // Limpia el input para que no suba nada
        return;
    }

    // Si todo está bien, previsualizar
    if (input.files && input.files.length > 0) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const div = document.createElement('div');
                div.className = "relative aspect-square animate-fade-in overflow-hidden rounded-2xl border border-slate-200 shadow-sm";

                div.innerHTML = `
                    <img src="${e.target.result}" class="w-full h-full object-cover">
                    <button type="button"
                            onclick="this.parentElement.remove(); actualizarContadorGaleria();"
                            class="absolute top-2 right-2 w-7 h-7 flex items-center justify-center bg-red-500/90 hover:bg-red-600 text-white rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-all duration-200 transform hover:scale-110 active:scale-95 z-10">
                        <i class="fas fa-trash-alt text-xs"></i>
                    </button>
                `;

                // Agregar hover para mostrar el botón eliminar en previsualización
                div.classList.add('group');

                container.appendChild(div);

                // Actualizar contador después de agregar
                actualizarContadorGaleria();
            };
            reader.readAsDataURL(file);
        });
    }

    // Actualizar contador inmediatamente (por si no carga rápido)
    actualizarContadorGaleria();
};

// Función auxiliar para actualizar contador (llámala siempre que cambie algo)
window.actualizarContadorGaleria = function() {
    const countLabel = document.getElementById('countGaleria');
    if (!countLabel) return;

    const galeriaCompleta = document.getElementById('galeriaCompleta');
    const existentes = galeriaCompleta ? galeriaCompleta.querySelectorAll('div.relative:not(#previewGaleria > *)').length : 0;
    const nuevas = document.getElementById('previewGaleria') ? document.getElementById('previewGaleria').children.length : 0;

    const total = existentes + nuevas;

    countLabel.innerText = `${total}/9`;

    // Cambiar estilo cuando llega al límite
    if (total >= 9) {
        countLabel.classList.remove('bg-blue-50', 'text-blue-600');
        countLabel.classList.add('bg-emerald-500', 'text-white');
        document.getElementById('btn-add-galeria')?.classList.add('hidden');
    } else {
        countLabel.classList.remove('bg-emerald-500', 'text-white');
        countLabel.classList.add('bg-blue-50', 'text-blue-600');
        document.getElementById('btn-add-galeria')?.classList.remove('hidden');
    }
};

// Llamar al contador al cargar la página (modo edición)
document.addEventListener('DOMContentLoaded', () => {
    actualizarContadorGaleria();
});

function eliminarImagen(btn) {
    const container = btn.closest('.image-container');
    container.remove();
}

// 3. Para el Video
function previewVideo(input) {
    if (input.files && input.files[0]) {
        // --- ESTO TE FALTABA: Definir las variables ---
        const parent = input.parentElement;
        const dropzone = parent.querySelector('div');
        const icon = parent.querySelector('i');
        const text = parent.querySelector('p');
        // ----------------------------------------------

        const nameLabel = document.getElementById('videoName');
        const container = document.getElementById('containerPreviewVideo');

        if (nameLabel) nameLabel.innerText = "🎥 " + input.files[0].name;
        if (container) container.classList.remove('hidden');

        // Efecto visual
        if (icon) {
            icon.className = "fa fa-check-circle text-3xl text-emerald-500 mb-2 animate-bounce";
        }

        if (dropzone) {
            // El video es AMBER, así que removemos amber y ponemos emerald
            dropzone.classList.remove('bg-amber-600/5', 'border-gray-200');
            dropzone.classList.add('bg-emerald-50', 'border-emerald-400');
        }

        if (text) {
            text.innerText = "¡VIDEO SELECCIONADO!";
            text.classList.remove('text-gray-400', 'text-amber-600');
            text.classList.add('text-emerald-600');
        }
    }
}