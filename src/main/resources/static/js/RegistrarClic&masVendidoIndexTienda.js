// Inicialización del carrusel de más vendidos
  $(document).ready(function(){
      $('.most-sold-carousel').slick({
          dots: true,
          infinite: true,
          speed: 300,
          slidesToShow: 3,
          slidesToScroll: 3,
          autoplay: true,
          autoplaySpeed: 2000,
          responsive: [
              {
                  breakpoint: 1200,
                  settings: {
                      slidesToShow: 3,
                      slidesToScroll: 3
                  }
              },
              {
                  breakpoint: 1024,
                  settings: {
                      slidesToShow: 2,
                      slidesToScroll: 2
                  }
              },
              {
                  breakpoint: 768,
                  settings: {
                      slidesToShow: 1,
                      slidesToScroll: 1
                  }
              },
              {
                  breakpoint: 640,
                  settings: {
                      slidesToShow: 1,
                      slidesToScroll: 1
                  }
              }
          ]
      });
  });

// Nueva función para registrar clics
  function logClick(productoId) {
      // Usa `fetch` para enviar una solicitud POST al controlador
      fetch('/productos/registrar-click', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: `productoId=${productoId}`
      })
      .then(response => {
          if (!response.ok) {
              console.error('Error al registrar el click:', response.statusText);
          } else {
              console.log('Click registrado exitosamente.');
          }
      })
      .catch(error => {
          console.error('Error de red al registrar el click:', error);
      });
  }
  async function handleCarritoSubmit(event, productoId) {
      event.preventDefault(); // Detenemos para asegurar el registro del click
      const form = event.target;
      const button = form.querySelector('button');

      // Feedback visual para el usuario
      if (button) {
          button.disabled = true;
          button.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Procesando...';
      }

      try {
          // 1. Registrar el clic en tu API de métricas
          await fetch('/productos/registrar-click', {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              body: `productoId=${encodeURIComponent(productoId)}`
          });
          console.log("✅ Click registrado");
      } catch (error) {
          console.error("⚠️ Error registrando click, procediendo igual:", error);
      }

      // 2. ENVIAR AL BACKEND Y REDIRECCIONAR
      // Enviamos el formulario programáticamente.
      // Tu controlador en Java recibirá el ID y el servidor hará el redirect a /carrito
      form.submit();
  }