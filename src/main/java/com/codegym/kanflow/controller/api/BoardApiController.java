package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/boards")
public class BoardApiController {

    @Autowired
    private IBoardService boardService;

    @Autowired
    private IUserService userService;

    // API để mời một thành viên mới vào board
    @PostMapping("/{boardId}/members")
    public ResponseEntity<String> addMemberToBoard(
            @PathVariable Long boardId,
            @RequestParam String username,
            Principal principal) {

        String resultMessage = boardService.inviteMember(boardId, username, principal.getName());

        // Kiểm tra thông báo trả về từ service để quyết định HttpStatus
        if (resultMessage.contains("successfully") || resultMessage.contains("added")) {
            return new ResponseEntity<>(resultMessage, HttpStatus.OK);
        } else if (resultMessage.contains("not found")) {
            return new ResponseEntity<>(resultMessage, HttpStatus.NOT_FOUND);
        } else if (resultMessage.contains("Only the board owner")) {
            return new ResponseEntity<>(resultMessage, HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(resultMessage, HttpStatus.BAD_REQUEST);
        }
    }
}
