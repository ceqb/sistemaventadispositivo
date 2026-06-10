package com.ceqb.SistemaVentaDispositivos2025.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {

    private final String BOT_TOKEN = "8372862682:AAGAqW0zXpjL8LouK_W453Cfy0KVJfDSRSw";
    private final String CHAT_ID = "657913651";

    public void enviarMensaje(String mensaje) {

        String url = "https://api.telegram.org/bot" + BOT_TOKEN +
                "/sendMessage?chat_id=" + CHAT_ID +
                "&text=" + mensaje;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(url, String.class);
    }
}
