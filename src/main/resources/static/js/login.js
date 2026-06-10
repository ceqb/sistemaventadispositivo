   document.addEventListener('DOMContentLoaded', function() {
        // Obtiene el URL actual
        const urlParams = new URLSearchParams(window.location.search);
        const showLoginModal = urlParams.get('showLogin');

        // Si el parámetro está presente, muestra el modal de login
        if (showLoginModal === 'true') {
            $('#loginModal').modal('show');
        }
    });