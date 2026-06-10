document.addEventListener("DOMContentLoaded", () => {
    const inputPC = document.getElementById("search-input-pc");
    const inputMobile = document.getElementById("search-input-mobile");
    const boxPC = document.getElementById("autocomplete-list-pc");
    const boxMobile = document.getElementById("autocomplete-list-mobile");

    function setupAutocomplete(input, box) {
        if (!input || !box) return;

        input.addEventListener("input", () => {
            const query = input.value.trim();
            if (query.length < 2) {
                box.innerHTML = "";
                box.classList.add("hidden");
                return;
            }

            fetch(`/tienda/sugerencias?query=${encodeURIComponent(query)}`)
                .then(r => r.json())
                .then(data => {
                    box.innerHTML = "";
                    box.classList.remove("hidden");

                    data.forEach(texto => {
                        const item = document.createElement("div");
                        item.className = "px-3 py-2 hover:bg-gray-100 cursor-pointer text-gray-700 text-sm";
                        item.textContent = texto;

                        item.onclick = () => {
                            input.value = texto;
                            box.classList.add("hidden");
                            // ¡ESTO ES LO NUEVO!: Envía el formulario al seleccionar
                            input.closest('form').submit();
                        };

                        box.appendChild(item);
                    });

                    if (data.length === 0) {
                        box.innerHTML = "<div class='px-3 py-2 text-gray-500'>Sin resultados</div>";
                    }
                })
                .catch(err => console.error("Error sugerencias:", err));
        });

        // Cerrar al hacer click fuera
        document.addEventListener("click", (e) => {
            if (!input.contains(e.target) && !box.contains(e.target)) {
                box.classList.add("hidden");
            }
        });
    }

    setupAutocomplete(inputPC, boxPC);
    setupAutocomplete(inputMobile, boxMobile);
});