 // Lógica para quitar el skeleton cuando la imagen carga
        document.querySelectorAll('.img-hidden').forEach(img => {
            if (img.complete) {
                img.classList.add('img-visible');
                const skeleton = img.closest('div').querySelector('.skeleton');
                if (skeleton) skeleton.remove();
            }
        });

                let currentIdx = 0;
            let mediaList = [];

            document.addEventListener("DOMContentLoaded", function() {
            // Llenar la lista de medios al cargar
            const thumbs = document.querySelectorAll('.thumb-item');
            thumbs.forEach((thumb, index) => {
            mediaList.push({
                type: thumb.getAttribute('data-type'),
                url: thumb.getAttribute('data-url'),
                element: thumb
            });
            // Si la miniatura tiene borde azul, marcar como índice actual
            if (thumb.classList.contains('border-blue-600')) {
                currentIdx = index;
            }
            });
            });

            function selectThumb(element) {
            const type = element.getAttribute('data-type');
            const url = element.getAttribute('data-url');
            changeMedia(type, url, element);
            }

            function changeMedia(type, url, element) {
                const container = document.getElementById('main-container');

                // Actualizar índice actual
                currentIdx = mediaList.findIndex(m => m.url === url);

                // Feedback visual en miniaturas
                document.querySelectorAll('.thumb-item').forEach(el => {
                    el.classList.remove('border-blue-600', 'border-2');
                    el.classList.add('border-gray-200');
                });
                element.classList.add('border-blue-600', 'border-2');

                // --- AQUÍ ESTÁ EL TRUCO ---
                // Al insertar el nuevo HTML, incluimos las clases: w-full, h-full, object-cover y rounded-[2rem]
                if (type === 'video') {
                    container.innerHTML = `<video class="w-full h-full object-contain bg-black rounded-[2rem]" controls autoplay muted loop>
                                                <source src="${url}" type="video/mp4">
                                           </video>`;
                } else {
                          container.classList.remove('bg-black'); // Fondo blanco para imágenes
                          container.innerHTML = `
                              <img src="${url}" class="w-full h-full object-contain animate-fade-in rounded-[2rem]">`;
                      }

                element.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
            }

            function nextMedia() {
            if (mediaList.length === 0) return;
            currentIdx = (currentIdx + 1) % mediaList.length;
            const next = mediaList[currentIdx];
            changeMedia(next.type, next.url, next.element);
            }

            function prevMedia() {
            if (mediaList.length === 0) return;
            currentIdx = (currentIdx - 1 + mediaList.length) % mediaList.length;
            const prev = mediaList[currentIdx];
            changeMedia(prev.type, prev.url, prev.element);
            }
