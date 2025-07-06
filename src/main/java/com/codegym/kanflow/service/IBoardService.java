package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;

import java.util.List;

public interface IBoardService {
    List<Board> findAll();
    void save(Board board);
    Board findById(Long id);
    Board findByIdWithDetails(Long id);

    List<Board> findAllByOwner(User owner);
}