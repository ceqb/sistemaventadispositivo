package com.ceqb.SistemaVentaDispositivos2025.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate;

    public TelegramService() {
        this.restTemplate = new RestTemplate();
    }

    @Async  // ← evita bloquear el hilo principal si Telegram tarda o falla
    public void enviarMensaje(String mensaje) {
        try {
            String url = "https://api.telegram.org/bot" + botToken +
                    "/sendMessage?chat_id=" + chatId +
                    "&text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // No relanzamos: Telegram es notificación opcional, no debe romper el flujo
            log.warn("No se pudo enviar mensaje a Telegram: {}", e.getMessage());
        }
    }
}
