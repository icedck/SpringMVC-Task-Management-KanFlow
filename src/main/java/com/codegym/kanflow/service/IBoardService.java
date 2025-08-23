package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;

import java.util.List;

public interface IBoardService {
    List<Board> findAll();

    void save(Board board);

    Board findById(Long id);

    Board findByIdWithDetails(Long id);

    List<Board> findByUser(User user);

    void deleteById(Long id);

    boolean hasAccess(Long boardId, String username);

    String inviteMember(Long boardId, String usernameToInvite, String currentUsername);

    Board findByIdWithOwner(Long id);

    String removeMember(Long boardId, Long userIdToRemove, String currentUsername);
}