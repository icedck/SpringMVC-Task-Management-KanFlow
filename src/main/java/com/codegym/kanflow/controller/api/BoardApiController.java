package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.UserDto;
import com.codegym.kanflow.dto.MemberJoinMessage;
import com.codegym.kanflow.dto.MemberLeaveMessage;
import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.IUserService;
import com.codegym.kanflow.service.IWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardApiController {

    @Autowired
    private IBoardService boardService;

    @Autowired
    private IUserService userService;
    
    @Autowired
    private IWebSocketService webSocketService;

    @PostMapping("/{boardId}/members")
    public ResponseEntity<String> addMemberToBoard(
            @PathVariable Long boardId,
            @RequestParam String email) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String resultMessage = boardService.inviteMember(boardId, email, username);

        if (resultMessage.contains("successfully") || resultMessage.contains("added")) {
            // Send WebSocket notification
            User newMember = userService.findByEmail(email);
            if (newMember != null) {
                MemberJoinMessage wsMessage = new MemberJoinMessage(
                    boardId, 
                    username, 
                    newMember.getId(), 
                    email, 
                    username + " đã mời " + newMember.getUsername() + " tham gia board"
                );
                webSocketService.sendToBoard(boardId, wsMessage);
            }
            return new ResponseEntity<>(resultMessage, HttpStatus.OK);
        } else if (resultMessage.contains("not found")) {
            return new ResponseEntity<>(resultMessage, HttpStatus.NOT_FOUND);
        } else if (resultMessage.contains("Only the board owner")) {
            return new ResponseEntity<>(resultMessage, HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(resultMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{boardId}/members")
    public ResponseEntity<List<UserDto>> getBoardMembers(@PathVariable Long boardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        if (!boardService.hasAccess(boardId, username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Board board = boardService.findByIdWithDetails(boardId);
        if (board == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<UserDto> memberDtos = new ArrayList<>();
        for (User user : board.getMembers()) {
            UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getEmail());
            memberDtos.add(dto);
        }

        return new ResponseEntity<>(memberDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/members/{userId}")
    public ResponseEntity<String> removeMemberFromBoard(
            @PathVariable Long boardId,
            @PathVariable Long userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String resultMessage = boardService.removeMember(boardId, userId, username);

        if (resultMessage.contains("removed")) {
            // Send WebSocket notification
            User removedUser = userService.findById(userId);
            if (removedUser != null) {
                MemberLeaveMessage wsMessage = new MemberLeaveMessage(
                    boardId, 
                    username, 
                    userId, 
                    username + " đã xóa " + removedUser.getUsername() + " khỏi board"
                );
                webSocketService.sendToBoard(boardId, wsMessage);
            }
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
