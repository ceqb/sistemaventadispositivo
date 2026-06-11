package com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaClientService {
    private final WebClient.Builder webClientBuilder;

    @Value("${AUDITORIA_SERVICE_URL:http://localhost:9010}/api/v1/auditoria/registrar")
    private String auditoriaUrl;

    public void registrar(String entidad, String accion, String descripcion) {
        AuditoriaDTO dto = AuditoriaDTO.builder()
                .entidad(entidad)
                .accion(accion)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .build();

        webClientBuilder.build()
                .post()
                .uri(auditoriaUrl)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
