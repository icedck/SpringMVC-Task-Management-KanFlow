package com.codegym.kanflow.controller;

import com.codegym.kanflow.dto.*;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IBoardService boardService;

    @Autowired
    private IUserService userService;

    @MessageMapping("/board/{boardId}/join")
    public void joinBoard(@DestinationVariable Long boardId, @Payload String username, SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = userService.findByUsername(username);
            if (user != null && boardService.hasAccess(boardId, username)) {
                // Send notification to all board members
                MemberJoinMessage message = new MemberJoinMessage(
                    boardId, 
                    username, 
                    user.getId(), 
                    user.getEmail(), 
                    username + " đã tham gia board"
                );
                
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
                
                
                // Send current user list to the joining user
                List<User> members = boardService.findByIdWithDetails(boardId).getMembers();
                List<UserDto> memberDtos = new ArrayList<>();
                for (User member : members) {
                    memberDtos.add(new UserDto(member.getId(), member.getUsername(), member.getEmail()));
                }
                
                messagingTemplate.convertAndSendToUser(username, "/queue/board/" + boardId + "/members", memberDtos);
                
                // Don't send status of existing members - they should send their own status when they connect
                // This prevents showing all members as "online" when they might not be
            }
        } catch (Exception e) {
            // Log error but don't crash the WebSocket connection
            System.err.println("Error in joinBoard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/leave")
    public void leaveBoard(@DestinationVariable Long boardId, @Payload String username, SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = userService.findByUsername(username);
            if (user != null) {
                MemberLeaveMessage message = new MemberLeaveMessage(
                    boardId, 
                    username, 
                    user.getId(), 
                    username + " đã rời khỏi board"
                );
                
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @MessageMapping("/board/{boardId}/card/create")
    public void createCard(@DestinationVariable Long boardId, @Payload CardCreateMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/card/update")
    public void updateCard(@DestinationVariable Long boardId, @Payload CardUpdateMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/card/move")
    public void moveCard(@DestinationVariable Long boardId, @Payload CardMoveMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/card/delete")
    public void deleteCard(@DestinationVariable Long boardId, @Payload CardDeleteMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/list/create")
    public void createList(@DestinationVariable Long boardId, @Payload ListCreateMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/list/update")
    public void updateList(@DestinationVariable Long boardId, @Payload ListUpdateMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/list/delete")
    public void deleteList(@DestinationVariable Long boardId, @Payload ListDeleteMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/typing")
    public void handleTyping(@DestinationVariable Long boardId, @Payload TypingMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                // Send to all users except the sender
                messagingTemplate.convertAndSend("/topic/board/" + boardId + "/typing", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/board/{boardId}/cursor")
    public void handleCursorPosition(@DestinationVariable Long boardId, @Payload CursorPositionMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth != null && boardService.hasAccess(boardId, auth.getName())) {
                // Send to all users except the sender
                messagingTemplate.convertAndSend("/topic/board/" + boardId + "/cursor", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Method to handle list move messages
    public void handleListMove(com.codegym.kanflow.dto.ListMoveMessage message) {
        // This method will be called from the frontend WebSocket handler
        // No need to implement here as it's handled by the API controller
    }
}
