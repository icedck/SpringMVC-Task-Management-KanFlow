package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.Card;
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
        // Bước 1: Lấy Board, Owner, và Members
        Board board = boardRepository.findByIdWithMembersAndOwner(id).orElse(null);

        if (board != null) {
            // Bước 2: Chủ động tải các collection con
            // Dùng .size() là một mẹo để buộc Hibernate thực thi query tải collection
            board.getCardLists().size();

            // Bước 3: Tải sâu hơn vào Card và Assignees của nó
            for (CardList list : board.getCardLists()) {
                for (Card card : list.getCards()) {
                    card.getAssignees().size(); // Buộc tải danh sách assignees cho mỗi card
                }
            }
        }
        return board;
    }

    @Override
    @Transactional(readOnly = true) // Đảm bảo có transaction để có thể fetch lazy
    public List<Board> findByUser(User user) {
        List<Board> boards = boardRepository.findBoardsByUser(user);
        // Duyệt qua danh sách để chủ động tải thông tin cần thiết
        for (Board board : boards) {
            // Chạm vào owner và members để buộc Hibernate tải chúng
            // Điều này sẽ chạy các query bổ sung nhưng trong cùng một transaction
            board.getOwner().getUsername(); // Lấy một thuộc tính bất kỳ của owner
            board.getMembers().size();      // Lấy size để tải danh sách members
        }
        return boards;
    }

    @Override
    @Transactional // Rất quan trọng! Transaction phải được bắt đầu từ đây.
    public void deleteById(Long id) {
        // Bước 1: Tìm board cần xóa. Dùng findById đơn giản là đủ.
        Board boardToDelete = boardRepository.findById(id).orElse(null);

        if (boardToDelete != null) {
            // Bước 2: Dọn dẹp mối quan hệ ManyToMany.
            // Xóa tất cả các thành viên khỏi danh sách members của board.
            // Hành động này sẽ khiến Hibernate xóa các bản ghi tương ứng
            // trong bảng trung gian `board_members`.
            boardToDelete.getMembers().clear();

            // Bước 3: Gọi lệnh xóa của repository.
            // Vì đã có cascade=ALL, orphanRemoval=true trên quan hệ với CardList,
            // Hibernate sẽ tự động xóa các CardList, và các CardList sẽ tự động
            // xóa các Card.
            boardRepository.delete(boardToDelete); // Dùng delete(entity) sẽ an toàn hơn deleteById(id)
        }
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
    public String inviteMember(Long boardId, String email, String currentUsername) {
        Board board = findById(boardId); // Chỉ cần findById() đơn giản
        User userToInvite = userRepository.findByEmail(email);
        User currentUser = userRepository.findByUsername(currentUsername);

        if (board == null || userToInvite == null) {
            return "Board or user with this email not found.";
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

    @Override
    public Board findByIdWithOwner(Long id) {
        return boardRepository.findByIdWithOwner(id).orElse(null);
    }

    @Override
    @Transactional
    public String removeMember(Long boardId, Long userIdToRemove, String currentUsername) {
        Board board = boardRepository.findById(boardId).orElse(null);
        if (board == null) {
            return "Board not found.";
        }

        User currentUser = userRepository.findByUsername(currentUsername);
        User userToRemove = userRepository.findById(userIdToRemove).orElse(null);

        if (userToRemove == null) {
            return "User to remove not found.";
        }

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            return "Only the board owner can remove members.";
        }

        if (board.getOwner().getId().equals(userToRemove.getId())) {
            return "The owner cannot be removed from the board.";
        }

        if (!board.getMembers().contains(userToRemove)) {
            return "User is not a member of this board.";
        }

        // === LOGIC MỚI: DỌN DẸP USER KHỎI CÁC CARD TRƯỚC KHI XÓA ===
        // Vòng lặp này duyệt qua tất cả các danh sách (To Do, In Progress, ...) trong board
        for (CardList list : board.getCardLists()) {
            // Duyệt qua tất cả các card trong mỗi danh sách
            for (Card card : list.getCards()) {
                // Xóa người dùng này khỏi danh sách người được phân công của card
                // .remove() sẽ không làm gì nếu người dùng không có trong danh sách
                card.getAssignees().remove(userToRemove);
            }
        }
        // ==============================================================

        // Tiến hành xóa thành viên khỏi board
        board.getMembers().remove(userToRemove);

        return userToRemove.getUsername() + " has been removed from the board.";
    }
}