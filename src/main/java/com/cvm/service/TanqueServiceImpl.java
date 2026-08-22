package com.cvm.service;
import com.cvm.model.Tanque;
import com.cvm.repository.TanqueRepository;
import com.cvm.service.ITanqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TanqueServiceImpl implements ITanqueService {

    @Autowired
    private TanqueRepository tanqueRepository;

    @Override
    public Tanque guardar(Tanque tanque) {
        return tanqueRepository.save(tanque);
    }

    @Override
    public List<Tanque> listarTodos() {
        return tanqueRepository.findAll();
    }
}