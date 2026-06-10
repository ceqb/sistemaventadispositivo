package com.auditoria_service.controlador;


import com.auditoria_service.dto.AuditoriaDTO;
import com.auditoria_service.servicio.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @PostMapping("/registrar")
    public ResponseEntity<Void> registrarAuditoria(@RequestBody AuditoriaDTO dto) {
        auditoriaService.registrar(dto);
        return ResponseEntity.ok().build();
    }
}
