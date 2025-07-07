package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.BoardRepository;
import com.codegym.kanflow.repository.UserRepository;
import com.codegym.kanflow.service.IBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService implements IBoardService { // Thêm "implements IBoardService"
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Override
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    // ... trong BoardService.java
    @Override
    public void save(Board board) {
        // Nếu đây là board mới tạo (chưa có ID)
        if (board.getId() == null) {
            User owner = board.getOwner();
            // Tạo một List mới và thêm owner vào làm member đầu tiên
            List<User> members = new ArrayList<>();
            members.add(owner);
            board.setMembers(members);
        }
        boardRepository.save(board);
    }

    @Override
    public Board findById(Long id) {
        return boardRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Board findByIdWithDetails(Long id) {
        // Bước 1: Lấy Board, Owner, và Members trong một câu query
        Board board = boardRepository.findByIdWithDetails(id).orElse(null);

        if (board != null) {
            // Bước 2: Chủ động tải CardLists (Hibernate sẽ chạy 1 query nữa)
            // Vì có @OrderBy("position ASC") trong Entity Board, danh sách này sẽ được sắp xếp
            // Dùng .size() là một "mẹo" để buộc Hibernate thực thi query tải collection
            board.getCardLists().size();

            // Bước 3: Chủ động tải Cards và Assignees cho mỗi CardList
            // Vì có @OrderBy trong Entity, các card và assignee cũng sẽ được sắp xếp
            for (CardList list : board.getCardLists()) {
                list.getCards().forEach(card -> card.getAssignees().size());
            }
        }

        return board;
    }

    @Override
    public List<Board> findByUser(User user) {
        return boardRepository.findBoardsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Long boardId, String username) {
        Board board = findById(boardId); // Dùng findById đơn giản là đủ
        if (board == null) {
            return false;
        }

        // Duyệt qua danh sách thành viên để kiểm tra
        for (User member : board.getMembers()) {
            if (member.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addMember(Board board, User user) {
        // Lấy danh sách thành viên hiện tại và thêm người mới vào.
        // Hibernate đủ thông minh để biết cần phải cập nhật bảng trung gian 'board_members'.
        board.getMembers().add(user);

        // Vì phương thức này được bọc trong @Transactional, chúng ta không cần
        // gọi boardRepository.save(board) một cách tường minh ở đây.
        // Khi transaction kết thúc, Hibernate sẽ tự động đồng bộ hóa thay đổi.
    }

    @Override
    @Transactional // Rất quan trọng!
    public String inviteMember(Long boardId, String usernameToInvite, String currentUsername) {
        Board board = findById(boardId); // Chỉ cần findById() đơn giản
        User userToInvite = userRepository.findByUsername(usernameToInvite);
        User currentUser = userRepository.findByUsername(currentUsername);

        if (board == null || userToInvite == null) {
            return "Board or user to invite not found.";
        }
        if (!board.getOwner().getId().equals(currentUser.getId())) {
            return "Only the board owner can invite members.";
        }
        // Bây giờ có thể truy cập getMembers() vì đang trong transaction
        if (board.getMembers().contains(userToInvite)) {
            return "User is already a member of this board.";
        }

        board.getMembers().add(userToInvite);
        // Không cần save() vì transaction sẽ commit thay đổi

        return userToInvite.getUsername() + " has been added to the board.";
    }
}