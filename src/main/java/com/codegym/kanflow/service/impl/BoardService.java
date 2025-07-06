package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.repository.BoardRepository;
import com.codegym.kanflow.service.IBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @Override
    @Transactional(readOnly = true) // Thêm readOnly = true cho các thao tác chỉ đọc
    public Board findByIdWithDetails(Long id) {
        // Bước 1: Lấy Board và các CardList của nó
        Board board = boardRepository.findByIdWithDetails(id).orElse(null);

        // Bước 2: Chủ động tải các Card cho mỗi CardList
        if (board != null) {
            // Dòng này sẽ buộc Hibernate phải thực hiện các câu query
            // để tải các Card cho mỗi CardList trong Board
            // Vì chúng ta đang ở trong một transaction, nên sẽ không có LazyInitializationException
            board.getCardLists().forEach(list -> list.getCards().size());
        }

        return board;
    }
}