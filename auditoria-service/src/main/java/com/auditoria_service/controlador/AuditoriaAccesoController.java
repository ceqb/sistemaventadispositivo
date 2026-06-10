package com.auditoria_service.controlador;

import com.auditoria_service.dto.AuditoriaAccesoDTO;
import com.auditoria_service.servicio.AuditoriaAccesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria-acceso")
@RequiredArgsConstructor
public class AuditoriaAccesoController {
    private final AuditoriaAccesoService service;

    @PostMapping("/registrar")
    public void registrarAcceso(@RequestBody AuditoriaAccesoDTO dto) {
        service.registrarAcceso(dto);
    }
}
