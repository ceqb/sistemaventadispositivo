package com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuditoriaAccesoService {


    @Value("${AUDITORIA_SERVICE_URL:http://localhost:9010}/api/v1/auditoria/registrar")
    private String auditoriaUrl;

    @Autowired
    private WebClient.Builder webClientBuilder;
    public void registrarAuditoriaAcceso(AuditoriaAccesoDTO dto) {

        webClientBuilder.build()
                .post()
                .uri(auditoriaUrl)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
