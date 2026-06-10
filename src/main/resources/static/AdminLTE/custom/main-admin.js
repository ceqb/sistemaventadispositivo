/**
 * main-admin.js
 * Script global para todo el panel administrativo.
 * Maneja el sidebar responsive, dropdowns, y utilidades comunes.
 */

// =========================
// 💠 SIDEBAR RESPONSIVE
// =========================
document.addEventListener("DOMContentLoaded", () => {

    const sidebarToggle = document.getElementById("sidebar-toggle");
    const sidebar = document.querySelector(".main-sidebar");

    // Solo si existe el sidebar
    if (sidebar) {
        const sidebarLinks = sidebar.querySelectorAll('a');

        sidebarLinks.forEach(link => {
            link.addEventListener("click", () => {
                // Cierra automáticamente en móvil
                if (window.innerWidth < 768) {
                    sidebar.classList.remove("mobile-open");
                }
            });
        });
    }


    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener("click", () => {
            sidebar.classList.toggle("mobile-open");
        });
    }


    // =========================
    // 💠 Dropdown del usuario (navbar)
    // =========================
    const userMenuBtn = document.querySelector(".group");
    if (userMenuBtn) {
        userMenuBtn.addEventListener("mouseenter", () => {
            const menu = userMenuBtn.querySelector("ul");
            if (menu) {
                menu.classList.add("opacity-100", "visible");
                menu.classList.remove("opacity-0", "invisible");
            }
        });

        userMenuBtn.addEventListener("mouseleave", () => {
            const menu = userMenuBtn.querySelector("ul");
            if (menu) {
                menu.classList.remove("opacity-100", "visible");
                menu.classList.add("opacity-0", "invisible");
            }
        });
    }
  /* ---------------------------------------------
       AJUSTES GLOBALES PARA ADMINLTE CUSTOM
    --------------------------------------------- */
    console.log("main-admin.js cargado correctamente ✔");
});
function toggleAdminSidebar() {
        const sidebar = document.getElementById('adminSidebar');
        const overlay = document.getElementById('sidebarOverlay');

        // Si el sidebar está oculto, lo muestra
        if (sidebar.classList.contains('-translate-x-full')) {
            sidebar.classList.remove('-translate-x-full');
            overlay.classList.remove('hidden');
        } else {
            sidebar.classList.add('-translate-x-full');
            overlay.classList.add('hidden');
        }
    }

function toggleUserDropdown() {
        const dropdown = document.getElementById('userDropdown');
        dropdown.classList.toggle('hidden');
    }





    // Cerrar el dropdown si el usuario hace clic fuera de él
    window.onclick = function(event) {
        if (!event.target.closest('#userDropdown') && !event.target.closest('button[onclick="toggleUserDropdown()"]')) {
            const dropdown = document.getElementById('userDropdown');
            if (!dropdown.classList.contains('hidden')) {
                dropdown.classList.add('hidden');
            }
        }
    }

 // --- INTERFAZ ---
     function toggleAdminSidebar() {
         document.getElementById('adminSidebar').classList.toggle('-translate-x-full');
         document.getElementById('sidebarOverlay').classList.toggle('hidden');
     }

     function toggleUserDropdown() {
         document.getElementById('userDropdown').classList.toggle('hidden');
     }