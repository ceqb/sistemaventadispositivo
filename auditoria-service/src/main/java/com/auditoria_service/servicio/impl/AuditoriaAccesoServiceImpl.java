package com.auditoria_service.servicio.impl;


import com.auditoria_service.dto.AuditoriaAccesoDTO;
import com.auditoria_service.model.Auditoria;
import com.auditoria_service.model.AuditoriaAcceso;
import com.auditoria_service.repositorio.AuditoriaAccesoRepository;
import com.auditoria_service.servicio.AuditoriaAccesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaAccesoServiceImpl implements AuditoriaAccesoService {

    private final AuditoriaAccesoRepository repository;

    @Override
    public void registrarAcceso(AuditoriaAccesoDTO dto) {
        repository.save(AuditoriaAcceso.toModel(dto));
    }
}
