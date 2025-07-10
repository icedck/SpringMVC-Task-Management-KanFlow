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

    /**
     * Thêm một User vào danh sách thành viên của một Board.
     *
     * @param board Board cần thêm thành viên.
     * @param user  User sẽ được thêm vào.
     */
    void addMember(Board board, User user);

    // Trả về String để chứa thông báo lỗi hoặc thành công
    String inviteMember(Long boardId, String usernameToInvite, String currentUsername);

    Board findByIdWithOwner(Long id);
}