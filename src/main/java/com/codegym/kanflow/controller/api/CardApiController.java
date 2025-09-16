package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.*;
import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.Label;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.model.Attachment;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.ICardListService;
import com.codegym.kanflow.service.ICardService;
import com.codegym.kanflow.service.IWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/cards")
public class CardApiController {

    @Autowired
    private ICardService cardService;
    @Autowired
    private ICardListService cardListService;
    @Autowired
    private IBoardService boardService;
    @Autowired
    private IWebSocketService webSocketService;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardDto cardDto, @RequestParam Long listId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        CardList cardList = cardListService.findByIdWithBoard(listId);
        if (cardList == null) {
            return new ResponseEntity<>("List not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(cardList.getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        Card newCard = new Card();
        newCard.setTitle(cardDto.getTitle());
        newCard.setCardList(cardList);
        Card savedCard = cardService.save(newCard);
        CardDto responseDto = new CardDto(savedCard.getId(), savedCard.getTitle(), null, 0, new ArrayList<>(), new HashSet<>());
        
        // Send WebSocket notification
        CardCreateMessage wsMessage = new CardCreateMessage(
            cardList.getBoard().getId(), 
            username, 
            savedCard.getId(), 
            savedCard.getTitle(), 
            cardList.getId(), 
            savedCard.getPosition()
        );
        webSocketService.sendToBoard(cardList.getBoard().getId(), wsMessage);
        
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{cardId}/members")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCardMembers(@PathVariable Long cardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        
        // Force load board and members to avoid lazy loading
        Long boardId = card.getCardList().getBoard().getId();
        if (!boardService.hasAccess(boardId, username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        
        // Get board with members using boardService to ensure proper loading
        Board board = boardService.findByIdWithDetails(boardId);
        List<User> boardMembers = board.getMembers();
        List<UserDto> memberDtos = new ArrayList<>();
        
        for (User member : boardMembers) {
            boolean assigned = card.getAssignees().contains(member);
            memberDtos.add(new UserDto(member.getId(), member.getUsername(), member.getEmail(), assigned));
        }
        
        return new ResponseEntity<>(memberDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCardDetails(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            throw new AccessDeniedException("You do not have permission to view this card.");
        }

        List<UserDto> assigneeDtos = new ArrayList<>();
        for (User user : card.getAssignees()) {
            assigneeDtos.add(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
        }

        Set<LabelDto> labelDtos = new HashSet<>();
        for (Label label : card.getLabels()) {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            labelDtos.add(dto);
        }

        CardDto responseDto = new CardDto(card.getId(), card.getTitle(), card.getDescription(), card.getPosition(), assigneeDtos, labelDtos);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable Long id, @RequestBody CardDto cardDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        
        // Get board ID before updating to avoid lazy loading issues
        Long boardId = card.getCardList().getBoard().getId();
        if (!boardService.hasAccess(boardId, username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        
        card.setTitle(cardDto.getTitle());
        card.setDescription(cardDto.getDescription());
        cardService.save(card);

        // Get fresh card data to avoid lazy loading issues
        Card freshCard = cardService.findByIdWithDetails(id);
        
        List<UserDto> assigneeDtos = new ArrayList<>();
        for (User user : freshCard.getAssignees()) {
            assigneeDtos.add(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
        }

        Set<LabelDto> labelDtos = new HashSet<>();
        for (Label label : freshCard.getLabels()) {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            labelDtos.add(dto);
        }

        CardDto responseDto = new CardDto(freshCard.getId(), freshCard.getTitle(), freshCard.getDescription(), freshCard.getPosition(), assigneeDtos, labelDtos);
        
        // Send WebSocket notification
        CardUpdateMessage wsMessage = new CardUpdateMessage(
            boardId, 
            username, 
            freshCard.getId(), 
            freshCard.getTitle(), 
            freshCard.getDescription(), 
            assigneeDtos, 
            labelDtos
        );
        webSocketService.sendToBoard(boardId, wsMessage);
        
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        // Send WebSocket notification before deletion
        CardDeleteMessage wsMessage = new CardDeleteMessage(
            card.getCardList().getBoard().getId(), 
            username, 
            card.getId(), 
            card.getTitle(), 
            card.getCardList().getId()
        );
        webSocketService.sendToBoard(card.getCardList().getBoard().getId(), wsMessage);
        
        cardService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{cardId}/move")
    public ResponseEntity<Void> moveCard(@PathVariable Long cardId, @RequestBody CardMoveDto cardMoveDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        CardList targetList = cardListService.findByIdWithBoard(cardMoveDto.getTargetListId());
        if (targetList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(targetList.getBoard().getId(), username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Long fromListId = card.getCardList().getId();
            String cardTitle = card.getTitle();
            
            cardService.move(card, targetList, cardMoveDto.getNewPosition());
            
            // Send WebSocket notification
            CardMoveMessage wsMessage = new CardMoveMessage(
                targetList.getBoard().getId(), 
                username, 
                card.getId(), 
                fromListId, 
                targetList.getId(), 
                cardMoveDto.getNewPosition(), 
                cardTitle
            );
            webSocketService.sendToBoard(targetList.getBoard().getId(), wsMessage);
            
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{cardId}/assignees/{userId}")
    public ResponseEntity<?> assignMember(@PathVariable Long cardId, @PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.assignMember(cardId, userId);
        
        // Get updated card with all details
        Card updatedCard = cardService.findByIdWithDetails(cardId);
        
        // Prepare assignees and labels for WebSocket
        List<UserDto> assigneeDtos = new ArrayList<>();
        for (User user : updatedCard.getAssignees()) {
            assigneeDtos.add(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
        }

        Set<LabelDto> labelDtos = new HashSet<>();
        for (Label label : updatedCard.getLabels()) {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            labelDtos.add(dto);
        }
        
        // Send WebSocket notification
        CardUpdateMessage wsMessage = new CardUpdateMessage(
            updatedCard.getCardList().getBoard().getId(), 
            username, 
            updatedCard.getId(), 
            updatedCard.getTitle(), 
            updatedCard.getDescription(), 
            assigneeDtos, 
            labelDtos
        );
        webSocketService.sendToBoard(updatedCard.getCardList().getBoard().getId(), wsMessage);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{cardId}/assignees/{userId}")
    public ResponseEntity<?> unassignMember(@PathVariable Long cardId, @PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.unassignMember(cardId, userId);
        
        // Get updated card with all details
        Card updatedCard = cardService.findByIdWithDetails(cardId);
        
        // Prepare assignees and labels for WebSocket
        List<UserDto> assigneeDtos = new ArrayList<>();
        for (User user : updatedCard.getAssignees()) {
            assigneeDtos.add(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
        }

        Set<LabelDto> labelDtos = new HashSet<>();
        for (Label label : updatedCard.getLabels()) {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            labelDtos.add(dto);
        }
        
        // Send WebSocket notification
        CardUpdateMessage wsMessage = new CardUpdateMessage(
            updatedCard.getCardList().getBoard().getId(), 
            username, 
            updatedCard.getId(), 
            updatedCard.getTitle(), 
            updatedCard.getDescription(), 
            assigneeDtos, 
            labelDtos
        );
        webSocketService.sendToBoard(updatedCard.getCardList().getBoard().getId(), wsMessage);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{cardId}/attachments")
    public ResponseEntity<?> getCardAttachments(@PathVariable Long cardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }

        List<AttachmentDto> attachmentDtos = new ArrayList<>();
        for (Attachment att : card.getAttachments()) {
            AttachmentDto dto = new AttachmentDto();
            dto.setId(att.getId());
            dto.setFileName(att.getFileName());
            dto.setUrl("/attachments/" + att.getStoredFileName());
            attachmentDtos.add(dto);
        }

        return new ResponseEntity<>(attachmentDtos, HttpStatus.OK);
    }

    @PostMapping("/{cardId}/labels/{labelId}")
    public ResponseEntity<?> assignLabel(@PathVariable Long cardId, @PathVariable Long labelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.assignLabel(cardId, labelId);
        
        // Get updated card with all details
        Card updatedCard = cardService.findByIdWithDetails(cardId);
        
        // Prepare assignees and labels for WebSocket
        List<UserDto> assigneeDtos = new ArrayList<>();
        for (User user : updatedCard.getAssignees()) {
            assigneeDtos.add(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
        }

        Set<LabelDto> labelDtos = new HashSet<>();
        for (Label label : updatedCard.getLabels()) {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            labelDtos.add(dto);
        }
        
        // Send WebSocket notification
        CardUpdateMessage wsMessage = new CardUpdateMessage(
            updatedCard.getCardList().getBoard().getId(), 
            username, 
            updatedCard.getId(), 
            updatedCard.getTitle(), 
            updatedCard.getDescription(), 
            assigneeDtos, 
            labelDtos
        );
        webSocketService.sendToBoard(updatedCard.getCardList().getBoard().getId(), wsMessage);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{cardId}/labels/{labelId}")
    public ResponseEntity<?> unassignLabel(@PathVariable Long cardId, @PathVariable Long labelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.unassignLabel(cardId, labelId);
        
        // Get updated card with all details
        Card updatedCard = cardService.findByIdWithDetails(cardId);
        
        // Prepare assignees and labels for WebSocket
        List<UserDto> assigneeDtos = new ArrayList<>();
        for (User user : updatedCard.getAssignees()) {
            assigneeDtos.add(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
        }

        Set<LabelDto> labelDtos = new HashSet<>();
        for (Label label : updatedCard.getLabels()) {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            labelDtos.add(dto);
        }
        
        // Send WebSocket notification
        CardUpdateMessage wsMessage = new CardUpdateMessage(
            updatedCard.getCardList().getBoard().getId(), 
            username, 
            updatedCard.getId(), 
            updatedCard.getTitle(), 
            updatedCard.getDescription(), 
            assigneeDtos, 
            labelDtos
        );
        webSocketService.sendToBoard(updatedCard.getCardList().getBoard().getId(), wsMessage);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }
}