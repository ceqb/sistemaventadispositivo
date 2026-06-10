package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;





import com.ceqb.SistemaVentaDispositivos2025.model.Carrito;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    // ✅ Inyecta la URL base con la anotación @Value
    @Value("${app.baseUrlCloudaflare}")
    private String baseUrlCloudaflare;


    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    // En tu MercadoPagoService
    // ✅ Firma del método con los argumentos necesarios
    public Preference createPreference(List<Carrito> itemsCarrito, Long usuarioId, Long pedidoId) throws MPException, MPApiException {
        PreferenceClient client = new PreferenceClient();
        List<PreferenceItemRequest> items = new ArrayList<>();

        for (Carrito item : itemsCarrito) {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(item.getProducto().getModelo_dpc())
                    .unitPrice(BigDecimal.valueOf(item.getProducto().getPrecio_dpc()))
                    .quantity(item.getCantidad())
                    .build();
            items.add(itemRequest);
        }

        // ✅ La URL del webhook debe ser tu URL de ngrok
        String notificationUrl = baseUrlCloudaflare + "/mercadopago/webhook";

        // ✅ Se añade el external_reference y metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("usuario_id", usuarioId);

        // ***********************************************************************
        // 1. Crea el objeto con tus URLs de retorno
        //************************************************************************
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(baseUrlCloudaflare + "/pedido-confirmado")
                .pending(baseUrlCloudaflare + "/pedido-pendiente")
                .failure(baseUrlCloudaflare + "/pedido-fallo")
                .build();

        PreferenceRequest request = PreferenceRequest.builder()
                .items(items)
                .externalReference(String.valueOf(pedidoId)) // 👈 ¡¡CLAVE!! Se añade el ID del pedido
                .metadata(metadata)
                .notificationUrl(notificationUrl)
                .backUrls(backUrls)
                .build();


        // Crear la preferencia en Mercado Pago
        Preference preference = client.create(request);

        return preference;
    }

    public Map<String, Object> getPaymentDetails(String paymentId) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.mercadopago.com")
                    .defaultHeader("Authorization", "Bearer " + accessToken)
                    .build();

            return webClient.get()
                    .uri("/v1/payments/{id}", paymentId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();
        } catch (Exception e) {
            System.err.println("Error obteniendo detalles del pago: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public String iniciarPago(Pedido pedido, List<Carrito> itemsCarrito) {
        try {
            // Llamamos directamente al método createPreference de esta misma clase
            Preference pref = createPreference(itemsCarrito, pedido.getUsuario().getId(), pedido.getId());
            pedido.setPreferenciaId(pref.getId());
            return pref.getInitPoint();
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error iniciando pago con MercadoPago", e);
        }
    }
    public void confirmarPago(Pedido pedido) {
        // Webhook de MercadoPago se encarga de la confirmación
    }

    public Preference getPreference(String preferenceId) {
        try {
            PreferenceClient client = new PreferenceClient();
            return client.get(preferenceId);
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al obtener la preferencia de pago con ID: " + preferenceId, e);
        }
    }
}