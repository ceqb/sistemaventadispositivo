async function toggleFavorite(btn) {
        const id = btn.getAttribute("data-id");

        try {
            const res = await fetch(`/favoritos/toggle/${id}`, {
                method: "POST"
            });

            // 1️⃣ Redirigido al login por Spring Security (302)
            if (res.redirected) {
                await Swal.fire({
                    icon: "warning",
                    title: "Debes iniciar sesión",
                    text: "Inicia sesión para agregar productos a favoritos.",
                    confirmButtonText: "Aceptar"
                });
                showLoginModal(); // ⬅ Igual que en tu checkout
                return;
            }

            // 2️⃣ Respuesta 401 explícita
            if (res.status === 401) {
                await Swal.fire({
                    icon: "warning",
                    title: "Debes iniciar sesión",
                    text: "Inicia sesión para agregar productos a favoritos.",
                    confirmButtonText: "Aceptar"
                });
                showLoginModal();
                return;
            }

            // 3️⃣ Verificar que la respuesta sea JSON
            const contentType = res.headers.get("content-type") || "";
            if (!contentType.includes("application/json")) {
                console.error("Respuesta NO JSON. Posible redirección oculta al login.");
                await Swal.fire({
                    icon: "warning",
                    title: "Debes iniciar sesión",
                    text: "Inicia sesión para agregar productos a favoritos.",
                    confirmButtonText: "Aceptar"
                });
                showLoginModal();
                return;
            }

            // 4️⃣ Convertir JSON real
            const data = await res.json();

            // 5️⃣ Manejo visual del botón ♥
            const icon = btn.querySelector("i");

            if (data.fav) {
                // ❤️ Agregado a favoritos
                icon.classList.remove("text-gray-400");
                icon.classList.add("text-red-500");

                icon.classList.add("heart-beat");
                setTimeout(() => icon.classList.remove("heart-beat"), 400);

                btn.setAttribute("data-fav", "true");
            } else {
                // 🤍 Favorito removido
                icon.classList.remove("text-red-500");
                icon.classList.add("text-gray-400");

                btn.setAttribute("data-fav", "false");
            }

        } catch (err) {
            console.error("Error favoritos:", err);

            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No se pudo actualizar tu favorito."
            });
        }
    }