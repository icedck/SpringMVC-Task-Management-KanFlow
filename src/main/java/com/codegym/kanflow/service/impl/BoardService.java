package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.repository.BoardRepository;
import com.codegym.kanflow.service.IBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService implements IBoardService { // Thêm "implements IBoardService"

    @Autowired
    private BoardRepository boardRepository;

    @Override
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Override
    public void save(Board board) {
        boardRepository.save(board);
    }

    @Override
    public Board findById(Long id) {
        return boardRepository.findById(id).orElse(null);
    }
}