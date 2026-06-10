document.addEventListener('DOMContentLoaded', () => {

    /* -----------------------------------------------------
        NAVBAR / MENÚ MÓVIL (Solo tienda)
    ----------------------------------------------------- */
    const navbarToggler = document.getElementById('navbar-toggler');
    const navbarMenu = document.getElementById('navbar-menu');

    if (navbarToggler && navbarMenu) {
        navbarToggler.addEventListener('click', () => {
            navbarMenu.classList.toggle('hidden');
        });
    }

    /* -----------------------------------------------------
        DROPDOWN DE CUENTA (Solo tienda)
    -----------------------------------------------------
    const accountDropdownBtn = document.getElementById('account-dropdown-btn');
    const accountDropdownMenu = document.getElementById('account-dropdown-menu');

    if (accountDropdownBtn && accountDropdownMenu) {
        accountDropdownBtn.addEventListener('click', (event) => {
            event.stopPropagation();
            accountDropdownMenu.classList.toggle('active');
        });

        document.addEventListener('click', (event) => {
            if (!accountDropdownBtn.contains(event.target) &&
                !accountDropdownMenu.contains(event.target)) {
                accountDropdownMenu.classList.remove('active');
            }
        });
    }*/
    const accountDropdownBtn = document.getElementById('account-dropdown-btn');
    const accountDropdownMenu = document.getElementById('account-dropdown-menu');

    if (accountDropdownBtn && accountDropdownMenu) {

        // Solo activar click en móviles
        if (window.innerWidth < 768) {
            accountDropdownBtn.addEventListener('click', (event) => {
                event.stopPropagation();
                accountDropdownMenu.classList.toggle('hidden');
            });

            document.addEventListener('click', (event) => {
                if (!accountDropdownBtn.contains(event.target) &&
                    !accountDropdownMenu.contains(event.target)) {
                    accountDropdownMenu.classList.add('hidden');
                }
            });
        }
    }

    /* -----------------------------------------------------
        LOGIN MODAL (Solo tienda)
    ----------------------------------------------------- */
    const loginModal = document.getElementById('loginModal');
    const redirectUrlInput = document.getElementById('redirectUrl');

    if (loginModal && redirectUrlInput) {
        window.showLoginModal = function () {
            loginModal.style.display = 'flex';
            redirectUrlInput.value = window.location.pathname + window.location.search;
            if (accountDropdownMenu) accountDropdownMenu.classList.remove('active');
        };

        window.hideLoginModal = function () {
            loginModal.style.display = 'none';
        };

        if (document.querySelector('p[th\\:if="${error}"].text-red-500')) {
            showLoginModal();
        }
    }

/* -----------------------------------------------------
   MODAL PRODUCTO (video + imágenes + info)
----------------------------------------------------- */

const productModal = document.getElementById('productModal');
const BASE_UPLOADS = '/uploads/';

/* =========================
   Utilidad: construir URL
========================= */
function buildImageUrl(img, folder = "") {
    if (!img || img === 'null' || img === "") return null;

    let clean = img.trim();

    // 1. Si es el video, ya trae su ruta completa desde el HTML (ej. /uploads/videos/...)
    if (clean.startsWith('http') || clean.startsWith('/api/') || clean.includes('/videos/')) {
        return clean;
    }

    // 2. Si no es video, construimos la ruta: BASE + CARPETA + NOMBRE
    // Esto es lo que resuelve el problema de la base de datos limpia
    const folderPath = folder ? folder + '/' : '';
    return BASE_UPLOADS + folderPath + clean;
}

/* =========================
   Mostrar modal (Versión con dimensiones fijas)
========================= */
window.showProductModal = function (element) {
    if (!productModal) return;

    const modalNombre = document.getElementById('modalNombre');
    const modalDescripcion = document.getElementById('modalDescripcion');
    const modalPrecio = document.getElementById('modalPrecio');
    const modalVideo = document.getElementById('modal-video');
    const modalImages = document.getElementById('modal-images');
    const actionContainer = document.getElementById('modal-actions');

    if (!modalVideo || !modalImages) return;

    // 1. Dimensiones fijas (Tu código original)
    modalVideo.style.height = window.innerWidth < 768 ? "280px" : "450px";
    modalVideo.style.display = "flex";
    modalVideo.style.alignItems = "center";
    modalVideo.style.justifyContent = "center";
    modalVideo.style.overflow = "hidden";
    modalVideo.className = "w-full bg-black rounded-2xl relative flex items-center justify-center overflow-hidden";

    /* ---------- Textos ---------- */
    modalNombre.textContent = element.dataset.nombre || '';
    modalDescripcion.textContent = element.dataset.descripcion || '';
    modalPrecio.textContent = `S/ ${parseFloat(element.dataset.precio || 0).toFixed(2)}`;

    /* ---------- Limpiar ---------- */
    modalVideo.innerHTML = '';
    modalImages.innerHTML = '';

    /* ---------- Datos ---------- */
        // Capturamos el folder que enviamos desde el HTML
        const folder = element.dataset.folder || '';
        const videoUrl = element.dataset.video;
        const principalImgRaw = element.dataset.imagen;
        const imagenesRaw = element.dataset.imagenes || '';
        const idProducto = element.dataset.id;
        const inventario = parseInt(element.dataset.inventario || 0);


        // PASAMOS EL FOLDER AQUÍ ->
        const principalImg = buildImageUrl(principalImgRaw, folder);

        let listaMiniaturas = [];
        if (imagenesRaw.trim() !== '') {
            // MAPEAMOS CADA IMAGEN DE LA GALERÍA CON EL FOLDER ->
            listaMiniaturas = imagenesRaw.split(',')
                .map(i => buildImageUrl(i.trim(), folder))
                .filter(Boolean);
        }

    if (principalImg && !listaMiniaturas.includes(principalImg)) {
        listaMiniaturas.unshift(principalImg);
    }

    /* ---------- Formulario Añadir producto al carrito en el modal ----------*/
        if (actionContainer) {
                if (inventario > 0) {
                    actionContainer.innerHTML = `
                       <form action="/carrito/agregar" method="post" onsubmit="handleCarritoSubmit(event, ${idProducto})">
                               <input type="hidden" name="id" value="${idProducto}">
                               <input type="hidden" name="cantidad" value="1">
                               <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded-xl font-bold hover:bg-blue-700 text-xs uppercase tracking-wider">
                                   Añadir al carrito
                               </button>
                       </form>
                    `;
                } else {
                    actionContainer.innerHTML = `
                        <button class="w-full bg-gray-400 text-white py-3 rounded-xl font-bold" disabled>
                            Sin stock
                        </button>
                    `;
                }
            }


/* =========================
       FUNCION: Inventario
    ========================= */
// 1. Capturamos el inventario (si es nulo o vacío, asumimos 0)
    /* =====================================================
       ESTADO DE INVENTARIO
    ===================================================== */
    const stockBadge = document.getElementById('stockBadge');
    const stockIcon = document.getElementById('stockIcon');
    const stockText = document.getElementById('stockText');
    const btnAccion = document.getElementById('modal-footer-btn');

    if (stockBadge && stockIcon && stockText) {
        if (inventario > 0) {
            stockBadge.className =
                "inline-block mt-2 px-2 py-1 bg-green-100 text-green-700 uppercase font-bold rounded text-[9px] border border-green-200";
            stockIcon.className = "fas fa-check-circle mr-1";
            stockText.textContent = "Inventario Disponible";

            if (btnAccion) {
                btnAccion.disabled = false;
                btnAccion.classList.remove('bg-gray-400');
                btnAccion.classList.add('bg-blue-600');
                btnAccion.innerHTML = '<i class="fab fa-whatsapp mr-2"></i> Consultar';
            }
        } else {
            stockBadge.className =
                "inline-block mt-2 px-2 py-1 bg-red-100 text-red-700 uppercase font-bold rounded text-[9px] border border-red-200";
            stockIcon.className = "fas fa-exclamation-triangle mr-1";
            stockText.textContent = "Agotado / Sin Inventario";

            if (btnAccion) {
                btnAccion.disabled = true;
                btnAccion.classList.remove('bg-blue-600');
                btnAccion.classList.add('bg-gray-400');
                btnAccion.innerHTML = 'Producto Agotado';
            }
        }
    }

/* -----------------------------------------------------
    ADMIN: PREVISUALIZACIÓN DE SUBIDA (Fotos y Videos)
----------------------------------------------------- */
function initAdminPreviews() {
    const inputFoto = document.getElementById('archivoFoto');
    const inputVideo = document.getElementById('archivoVideo');
    const inputGaleria = document.getElementById('archivoInput');

    // Preview Foto Principal
    if (inputFoto) {
        inputFoto.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const previewContainer = document.getElementById('containerPreviewPrincipal');
                    const img = document.getElementById('imgPreview');
                    if(img) img.src = e.target.result;
                    if(previewContainer) previewContainer.classList.remove('hidden');
                };
                reader.readAsDataURL(this.files[0]);
            }
        });
    }

    // Preview Video (Feedback de nombre)
    if (inputVideo) {
        inputVideo.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const videoContainer = document.getElementById('containerPreviewVideo');
                const videoName = document.getElementById('videoName');
                if(videoName) videoName.innerText = "🎥 Seleccionado: " + this.files[0].name;
                if(videoContainer) videoContainer.classList.remove('hidden');

                // Feedback visual en el icono
                const icon = this.parentElement.querySelector('i');
                if(icon) icon.className = "fa fa-check-circle text-3xl text-emerald-500 mb-2 animate-bounce";
            }
        });
    }
}

// Ejecutar al cargar
document.addEventListener('DOMContentLoaded', initAdminPreviews);

    /* =========================
       FUNCION: Actualizar Visor sin mover el Layout
    ========================= */
    const updateMainDisplay = (sourceUrl, isVideo = false) => {
        modalVideo.style.opacity = '0.5';

        setTimeout(() => {
            modalVideo.innerHTML = '';
            if (isVideo || (sourceUrl && (sourceUrl.endsWith('.mp4') || sourceUrl.endsWith('.webm')))) {
                const videoElement = document.createElement('video');
                videoElement.src = sourceUrl;
                videoElement.autoplay = true;
                videoElement.muted = true;
                videoElement.loop = true;
                videoElement.controls = true;

                // --- ESTO SOLUCIONA EL SALTO A PANTALLA COMPLETA
                /*
                videoElement.setAttribute('playsinline', '');
                videoElement.setAttribute('webkit-playsinline', '');
                videoElement.setAttribute('x5-playsinline', '');  Para navegadores basados en motores chinos
                */

                // IMPORTANTE: max-h-full asegura que no se salga del contenedor en móvil
                videoElement.className = 'w-full h-full max-h-full object-contain';
                modalVideo.appendChild(videoElement);
            } else {
                const imgElement = document.createElement('img');
                imgElement.src = sourceUrl || (BASE_UPLOADS + 'no-image.png');
                imgElement.className = 'w-full h-full max-h-full object-contain p-2';
                imgElement.onerror = () => { imgElement.src = BASE_UPLOADS + 'no-image.png'; };
                modalVideo.appendChild(imgElement);
            }
            modalVideo.style.opacity = '1';
        }, 50);
    };

    /* =========================
       Carga Inicial
    ========================= */
       const tieneVideo = videoUrl && videoUrl.trim() !== '' && videoUrl !== 'null';

       if (tieneVideo) {
           updateMainDisplay(videoUrl, true);
       } else {
           updateMainDisplay(principalImg, false);
       }

    /* =========================
       Grid de Miniaturas (9 celdas fijas)
    ========================= */
    const todasLasFuentes = [];
    if (videoUrl && videoUrl !== 'null' && videoUrl !== '') {
        todasLasFuentes.push({ url: videoUrl, isVideo: true });
    }
    listaMiniaturas.forEach(url => todasLasFuentes.push({ url: url, isVideo: false }));

    for (let i = 0; i < 9; i++) {
        const imgContainer = document.createElement('div');

        // CAMBIO: Usamos h-full y rounded-xl para mejorar el estilo
        imgContainer.className = "relative h-full rounded-xl border overflow-hidden transition-all flex items-center justify-center bg-white shadow-sm";

        const item = todasLasFuentes[i];

        if (item) {
            imgContainer.className += " cursor-pointer hover:border-blue-500";

            if (item.isVideo) {
                const thumbVideo = document.createElement('video');
                thumbVideo.src = item.url;
                thumbVideo.className = 'w-full h-full object-cover opacity-60';
                imgContainer.appendChild(thumbVideo);
                imgContainer.insertAdjacentHTML('beforeend', '<div class="absolute inset-0 flex items-center justify-center"><i class="fas fa-play text-gray-800"></i></div>');
            } else {
                const imgElement = document.createElement('img');
                imgElement.src = item.url;
                // IMPORTANTE: w-full h-full object-cover evita que la imagen se estire o se pegue
                imgElement.className = 'w-full h-full object-cover';
                imgElement.onerror = () => { imgElement.src = BASE_UPLOADS + 'no-image.png'; };
                imgContainer.appendChild(imgElement);
            }

            imgContainer.onclick = () => updateMainDisplay(item.url, item.isVideo);
        } else {
            // Celda vacía con diseño más limpio
            imgContainer.className += " border-dashed border-gray-200 opacity-30";
            imgContainer.innerHTML = `<i class="fas fa-image text-[10px] text-gray-400"></i>`;
        }

        modalImages.appendChild(imgContainer);
    }

    /* ---------- Mostrar ---------- */
    productModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
};
/* =========================
   Cerrar modal
========================= */
window.hideProductModal = function () {
    if (!productModal) return;

    productModal.querySelectorAll('video').forEach(v => {
        v.pause();
        v.removeAttribute('src'); // Mejor que v.src = ''
        v.load();
        v.remove();
    });

    productModal.style.display = 'none';
    document.body.style.overflow = 'auto';
};


    /* -----------------------------------------------------
        BOTÓN VOLVER ARRIBA (Tienda y catálogo)
    ----------------------------------------------------- */
    const backToTopButton = document.getElementById('back-to-top');

    if (backToTopButton) {
        window.addEventListener('scroll', () => {
            backToTopButton.style.display = window.scrollY > 300 ? 'block' : 'none';
        });

        backToTopButton.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

});

/* -----------------------------------------------------
    MODAL DIRECCIÓN DE ENTREGA (Solo checkout)
----------------------------------------------------- */
const modalDireccion = document.getElementById("modalDireccion");
const modalBox = document.getElementById("modalBox");
const btnPagar = document.getElementById("pagoContraEntregaForm");
const btnConfirmar = document.getElementById("btnConfirmarDireccionModal");
const btnCancelar = document.getElementById("btnCancelarModal");

if (modalDireccion && modalBox && btnCancelar && btnConfirmar) {

    function showCustomModal() {
        modalDireccion.classList.remove("hidden");
        setTimeout(() => {
            modalBox.classList.remove("opacity-0", "scale-95");
            modalBox.classList.add("opacity-100", "scale-100");
        }, 10);
    }

    function hideCustomModal() {
        modalBox.classList.add("opacity-0", "scale-95");
        modalBox.classList.remove("opacity-100", "scale-100");
        setTimeout(() => modalDireccion.classList.add("hidden"), 150);
    }

    if (btnPagar) {
        btnPagar.addEventListener("click", () => {
            document.getElementById("errorDireccionModal").classList.add("hidden");
            document.getElementById("direccionModalInput").value = "";
            showCustomModal();
        });
    }

    btnCancelar.addEventListener("click", hideCustomModal);

    btnConfirmar.addEventListener("click", async () => {
        const direccion = document.getElementById("direccionModalInput").value.trim();
        const error = document.getElementById("errorDireccionModal");

        if (!direccion) {
            error.classList.remove("hidden");
            return;
        }

        try {
            const resp = await fetch("/pedidos/createPagoContraEntrega", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ direccion })
            });

            if (!resp.ok) throw new Error();

            const data = await resp.json();
            hideCustomModal();
            // 🟦 Reutilización detectada
                         if (data.mensaje && data.mensaje.includes("reutilizó")) {

                             Swal.fire({
                                 icon: "info",
                                 title: "Pedido Reutilizados",
                                 text: data.mensaje
                             }).then(() => {
                                 window.location.href = data.redirectUrl;
                             });

                             return;
                         }
            Swal.fire({
                icon: "success",
                title: "Pedido Confirmado",
                text: "Tu pedido fue registrado."
            }).then(() => {
                window.location.href = data.redirectUrl;
            });

        } catch {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudo procesar tu pedido."
            });
        }
    });
}
