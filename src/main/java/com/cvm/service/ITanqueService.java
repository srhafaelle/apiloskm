package com.cvm.service;
import com.cvm.model.Tanque;

import java.util.List;

public interface ITanqueService {
    Tanque guardar(Tanque tanque);
    List<Tanque> listarTodos();
}
