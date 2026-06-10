package com.auditoria_service.servicio.impl;

import com.auditoria_service.dto.AuditoriaDTO;
import com.auditoria_service.repositorio.AuditoriaRepository;
import com.auditoria_service.servicio.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.auditoria_service.model.Auditoria;
@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository repository;

    @Override
    public void registrar(AuditoriaDTO dto) {
        repository.save(Auditoria.toModel(dto));
    }
}
