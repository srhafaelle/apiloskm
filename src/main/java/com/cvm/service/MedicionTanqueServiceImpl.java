package com.cvm.service;
import com.cvm.model.MedicionTanque;
import com.cvm.repository.MedicionTanqueRepository;
import com.cvm.service.IMedicionTanqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MedicionTanqueServiceImpl implements IMedicionTanqueService {

    @Autowired
    private MedicionTanqueRepository repository;

    @Override
    public MedicionTanque guardar(MedicionTanque medicion) {
        // Si Flutter no envía fecha, la generamos como String
        if (medicion.getFechaMedicion() == null || medicion.getFechaMedicion().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            medicion.setFechaMedicion(LocalDateTime.now().format(formatter));
        }
        return repository.save(medicion);
    }

    @Override
    public List<MedicionTanque> obtenerHistorial(String inicio, String fin) {
        // Añadimos las horas para que abarque el día completo en formato String
        String start = inicio + "T00:00:00";
        String end = fin + "T23:59:59";
        return repository.findByFechaMedicionBetweenOrderByFechaMedicionDesc(start, end);
    }
}