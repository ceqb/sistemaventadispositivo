package com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaClientService {
    private final WebClient.Builder webClientBuilder;

    private static final String AUDITORIA_URL = "http://localhost:9010/api/v1/auditoria/registrar";

    public void registrar(String entidad, String accion, String descripcion) {
        AuditoriaDTO dto = AuditoriaDTO.builder()
                .entidad(entidad)
                .accion(accion)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .build();

        webClientBuilder.build()
                .post()
                .uri(AUDITORIA_URL)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
