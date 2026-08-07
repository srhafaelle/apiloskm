package com.cvm.service;

import com.cvm.model.Mina;
import com.cvm.model.SectorMinero;
import com.cvm.repository.MinaRepository;
import com.cvm.repository.SectorMineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService{


    private final SectorMineroRepository sectorRepository;
    private final MinaRepository minaRepository;

    public SectorMinero crearSector(SectorMinero sector) {
        return sectorRepository.save(sector);
    }

    public List<SectorMinero> obtenerTodosLosSectores() {
        return sectorRepository.findAll();
    }

    public Mina crearMina(Mina mina) {
        return minaRepository.save(mina);
    }

    public List<Mina> obtenerMinasPorSector(String sectorId) {
        return minaRepository.findBySectorMineroId(sectorId);
    }

    @Override
    public SectorService findBySectorId(String sectorId) {
        return null;
    }
}
