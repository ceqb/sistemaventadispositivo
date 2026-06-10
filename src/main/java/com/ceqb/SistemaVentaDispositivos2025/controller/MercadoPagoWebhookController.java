package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.repository.PedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl.MercadoPagoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookController.class);
    private final MercadoPagoService mercadoPagoService;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final UsuarioRepository usuarioRepository;


    // Usa @RequestMapping para manejar tanto solicitudes GET como POST

    @PostMapping("/webhook")
    public ResponseEntity<String> recibirWebhook(@RequestBody(required = false) String rawJson) {
        try {
            if (rawJson == null || rawJson.isBlank()) {
                //logger.error("❌ Webhook recibido sin body");
                return ResponseEntity.badRequest().body("Body vacío");
            }

            //logger.info("📥 Webhook RAW: {}", rawJson);

            // Convertir JSON → Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(rawJson, Map.class);

            //logger.info("📩 Payload parseado: {}", payload);

            // Pasar todo el payload al servicio
            pedidoService.procesarWebhookDePago(payload);

            return ResponseEntity.ok("Webhook procesado con éxito");

        } catch (Exception e) {
            //logger.error("❌ Error en Webhook", e);
            return ResponseEntity.status(500).body("Error en Webhook: " + e.getMessage());
        }
    }


}
