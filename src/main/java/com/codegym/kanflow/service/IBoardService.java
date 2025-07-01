package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Board;
import java.util.List;

public interface IBoardService {
    List<Board> findAll();
    void save(Board board);
    Board findById(Long id);
    Board findByIdWithDetails(Long id);
}