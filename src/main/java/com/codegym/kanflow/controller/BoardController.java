package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired private IBoardService boardService;
    @Autowired private IUserService userService;

    // Hiển thị danh sách tất cả board
    @GetMapping
    public ModelAndView showBoardList(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("board/list");

        // 1. Tìm người dùng hiện tại
        User currentUser = userService.findByUsername(principal.getName());
        Long currentUserId = currentUser.getId(); // Lấy ID ra một lần để so sánh

        // 2. Lấy TẤT CẢ các board mà người dùng có liên quan
        List<Board> allUserBoards = boardService.findByUser(currentUser);

        // 3. Chuẩn bị hai danh sách rỗng để chứa kết quả phân loại
        List<Board> ownedBoards = new ArrayList<>();
        List<Board> joinedBoards = new ArrayList<>();

        // 4. Dùng vòng lặp for-each để phân loại
        for (Board board : allUserBoards) {
            // Kiểm tra xem ID của owner có trùng với ID người dùng hiện tại không
            if (board.getOwner() != null && board.getOwner().getId().equals(currentUserId)) {
                // Nếu trùng, đây là board sở hữu
                ownedBoards.add(board);
            } else {
                // Nếu không trùng, đây là board đã tham gia
                joinedBoards.add(board);
            }
        }

        // 5. Đưa cả hai danh sách đã phân loại vào model
        modelAndView.addObject("ownedBoards", ownedBoards);
        modelAndView.addObject("joinedBoards", joinedBoards);

        return modelAndView;
    }

    // Hiển thị form tạo board
    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("board/create");
        modelAndView.addObject("board", new Board());
        return modelAndView;
    }

    @PostMapping("/create")
    // Sửa lại chữ ký phương thức để nhận Principal
    public String createBoard(@ModelAttribute("board") Board board, Principal principal) {
        // Lấy username của người dùng đang đăng nhập
        String username = principal.getName();
        // Tìm đối tượng User tương ứng trong CSDL
        User currentUser = userService.findByUsername(username);

        // Gán owner cho board
        board.setOwner(currentUser);

        boardService.save(board);
        return "redirect:/boards";
    }

    @GetMapping("/{id}")
    public ModelAndView showBoardDetails(@PathVariable Long id, Principal principal) {
// Kiểm tra quyền truy cập trước khi làm bất cứ điều gì
        if (!boardService.hasAccess(id, principal.getName())) {
            // Có thể trả về trang lỗi "Access Denied"
            throw new AccessDeniedException("You do not have permission to view this board.");
        }
        Board board = boardService.findByIdWithDetails(id);

        if (board != null) {
            ModelAndView modelAndView = new ModelAndView("board/detail");
            modelAndView.addObject("board", board);
            return modelAndView;
        } else {
            // Có thể tạo một file error/404.html đơn giản
            return new ModelAndView("error/404");
        }
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id, Principal principal) {
        // SỬ DỤNG PHƯƠNG THỨC MỚI
        Board board = boardService.findByIdWithOwner(id);

        // --- Kiểm tra quyền ---
        if (board == null || !board.getOwner().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this board.");
        }

        ModelAndView modelAndView = new ModelAndView("board/edit");
        modelAndView.addObject("board", board);
        return modelAndView;
    }

    // ===== PHƯƠNG THỨC SỬA (XỬ LÝ SUBMIT) =====
    @PostMapping("/edit")
    public String updateBoard(@ModelAttribute("board") Board board, Principal principal) {
        // SỬ DỤNG PHƯƠNG THỨC MỚI Ở ĐÂY NỮA
        Board existingBoard = boardService.findByIdWithOwner(board.getId());

        // --- Kiểm tra quyền ---
        if (existingBoard == null || !existingBoard.getOwner().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this board.");
        }

        existingBoard.setTitle(board.getTitle());
        boardService.save(existingBoard);
        return "redirect:/boards";
    }

    @PostMapping("/delete/{id}")
    public String deleteBoard(@PathVariable Long id, Principal principal) {
        // SỬA Ở ĐÂY: Dùng findByIdWithOwner để kiểm tra quyền
        Board board = boardService.findByIdWithOwner(id);

        // --- Kiểm tra quyền ---
        if (board == null || !board.getOwner().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You do not have permission to delete this board.");
        }

        // Gọi service để xóa
        boardService.deleteById(id);

        return "redirect:/boards";
    }
}