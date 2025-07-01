package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired private IBoardService boardService;
    @Autowired private IUserService userService;

    // Hiển thị danh sách tất cả board
    @GetMapping
    public ModelAndView showBoardList() {
        ModelAndView modelAndView = new ModelAndView("board/list");
        List<Board> boards = boardService.findAll();
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

    // Xử lý việc tạo board
    @PostMapping("/create")
    public String createBoard(@ModelAttribute("board") Board board) {
        // Tạm thời gán board cho user có id = 1
        // Sẽ thay đổi khi có chức năng đăng nhập
        User currentUser = userService.findById(1L);
        if (currentUser == null) {
            // Xử lý trường hợp không có user id=1 (ví dụ: quay về trang lỗi)
            // Tạm thời, để đơn giản, chúng ta bỏ qua. Cần đảm bảo có user id=1 trong CSDL.
            return "redirect:/error";
        }
        board.setOwner(currentUser);
        boardService.save(board);
        return "redirect:/boards";
    }

    @GetMapping("/{id}")
    public ModelAndView showBoardDetails(@PathVariable Long id) {
        // Thay đổi lời gọi ở đây
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