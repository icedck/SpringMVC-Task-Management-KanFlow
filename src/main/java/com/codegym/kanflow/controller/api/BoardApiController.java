package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.UserDto;
import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

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

    // === API MỚI ĐỂ LẤY DANH SÁCH THÀNH VIÊN ===
    @GetMapping("/{boardId}/members")
    public ResponseEntity<List<UserDto>> getBoardMembers(@PathVariable Long boardId, Principal principal) {
        // Kiểm tra xem người dùng hiện tại có quyền truy cập board này không
        if (!boardService.hasAccess(boardId, principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Dùng findByIdWithDetails để đảm bảo danh sách members đã được tải
        Board board = boardService.findByIdWithDetails(boardId);
        if (board == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Chuyển đổi danh sách User (Entity) thành danh sách UserDto
        List<UserDto> memberDtos = board.getMembers().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(memberDtos, HttpStatus.OK);
    }
}
