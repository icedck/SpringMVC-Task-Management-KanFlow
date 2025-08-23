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

    @GetMapping
    public ModelAndView showBoardList(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("board/list");

        User currentUser = userService.findByUsername(principal.getName());
        Long currentUserId = currentUser.getId(); // Lấy ID ra một lần để so sánh

        modelAndView.addObject("currentUser", currentUser);

        List<Board> allUserBoards = boardService.findByUser(currentUser);

        List<Board> ownedBoards = new ArrayList<>();
        List<Board> joinedBoards = new ArrayList<>();

        for (Board board : allUserBoards) {
            if (board.getOwner() != null && board.getOwner().getId().equals(currentUserId)) {
                ownedBoards.add(board);
            } else {
                joinedBoards.add(board);
            }
        }

        modelAndView.addObject("ownedBoards", ownedBoards);
        modelAndView.addObject("joinedBoards", joinedBoards);

        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("board/create");
        modelAndView.addObject("board", new Board());
        return modelAndView;
    }

    @PostMapping("/create")
    public String createBoard(@ModelAttribute("board") Board board, Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);

        board.setOwner(currentUser);

        boardService.save(board);
        return "redirect:/boards";
    }

    @GetMapping("/{id}")
    public ModelAndView showBoardDetails(@PathVariable Long id, Principal principal) {
        if (!boardService.hasAccess(id, principal.getName())) {
            throw new AccessDeniedException("You do not have permission to view this board.");
        }
        Board board = boardService.findByIdWithDetails(id);

        if (board != null) {
            ModelAndView modelAndView = new ModelAndView("board/detail");
            modelAndView.addObject("board", board);
            return modelAndView;
        } else {
            return new ModelAndView("error/404");
        }
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable Long id, Principal principal) {
        Board board = boardService.findByIdWithOwner(id);

        if (board == null || !board.getOwner().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this board.");
        }

        ModelAndView modelAndView = new ModelAndView("board/edit");
        modelAndView.addObject("board", board);
        return modelAndView;
    }

    @PostMapping("/edit")
    public String updateBoard(@ModelAttribute("board") Board board, Principal principal) {
        Board existingBoard = boardService.findByIdWithOwner(board.getId());

        if (existingBoard == null || !existingBoard.getOwner().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this board.");
        }

        existingBoard.setTitle(board.getTitle());
        boardService.save(existingBoard);
        return "redirect:/boards";
    }

    @PostMapping("/delete/{id}")
    public String deleteBoard(@PathVariable Long id, Principal principal) {
        Board board = boardService.findByIdWithOwner(id);

        if (board == null || !board.getOwner().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("You do not have permission to delete this board.");
        }

        boardService.deleteById(id);

        return "redirect:/boards";
    }
}