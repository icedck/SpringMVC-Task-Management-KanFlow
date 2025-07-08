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
import java.util.List;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired private IBoardService boardService;
    @Autowired private IUserService userService;

    // Hiển thị danh sách tất cả board
    @GetMapping
    public ModelAndView showBoardList(Principal principal) { // Nhận Principal
        ModelAndView modelAndView = new ModelAndView("board/list");

        // Tìm người dùng hiện tại
        User currentUser = userService.findByUsername(principal.getName());

        // Lấy danh sách các board chỉ thuộc về người dùng này
        List<Board> boards = boardService.findByUser(currentUser);

        modelAndView.addObject("boards", boards);
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
}