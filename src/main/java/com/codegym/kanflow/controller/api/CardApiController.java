package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.*;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.ICardListService;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
public class CardApiController {

    @Autowired
    private ICardService cardService;
    @Autowired
    private ICardListService cardListService;
    @Autowired
    private IBoardService boardService;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardDto cardDto, @RequestParam Long listId, Principal principal) {
        // Dùng findByIdWithBoard để lấy luôn board, tránh Lazy
        CardList cardList = cardListService.findByIdWithBoard(listId);
        if (cardList == null) {
            return new ResponseEntity<>("List not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(cardList.getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        Card newCard = new Card();
        newCard.setTitle(cardDto.getTitle());
        newCard.setCardList(cardList);
        Card savedCard = cardService.save(newCard);
        CardDto responseDto = new CardDto(savedCard.getId(), savedCard.getTitle(), null, 0, new ArrayList<>());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCardDetails(@PathVariable Long id, Principal principal) {
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            throw new AccessDeniedException("You do not have permission to view this card.");
        }
        List<UserDto> assigneeDtos = card.getAssignees().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());

        // === BẠN ĐANG THIẾU ĐOẠN NÀY ===
        Set<LabelDto> labelDtos = card.getLabels().stream()
                .map(label -> {
                    LabelDto dto = new LabelDto();
                    dto.setId(label.getId());
                    dto.setName(label.getName());
                    dto.setColor(label.getColor());
                    return dto;
                })
                .collect(Collectors.toSet());

        // Sử dụng constructor mới của CardDto
        CardDto responseDto = new CardDto(card.getId(), card.getTitle(), card.getDescription(), card.getPosition(), assigneeDtos, labelDtos);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable Long id, @RequestBody CardDto cardDto, Principal principal) {
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        card.setTitle(cardDto.getTitle());
        card.setDescription(cardDto.getDescription());
        Card updatedCard = cardService.save(card);
        List<UserDto> assigneeDtos = updatedCard.getAssignees().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());

        // === BẠN ĐANG THIẾU ĐOẠN NÀY ===
        Set<LabelDto> labelDtos = updatedCard.getLabels().stream()
                .map(label -> {
                    LabelDto dto = new LabelDto();
                    dto.setId(label.getId());
                    dto.setName(label.getName());
                    dto.setColor(label.getColor());
                    return dto;
                })
                .collect(Collectors.toSet());

        // Sử dụng constructor mới của CardDto
        CardDto responseDto = new CardDto(updatedCard.getId(), updatedCard.getTitle(), updatedCard.getDescription(), updatedCard.getPosition(), assigneeDtos, labelDtos);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id, Principal principal) {
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        cardService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{cardId}/move")
    public ResponseEntity<Void> moveCard(@PathVariable Long cardId, @RequestBody CardMoveDto cardMoveDto, Principal principal) {
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        CardList targetList = cardListService.findByIdWithBoard(cardMoveDto.getTargetListId());
        if (targetList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(targetList.getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            cardService.move(card, targetList, cardMoveDto.getNewPosition());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{cardId}/assignees/{userId}")
    public ResponseEntity<?> assignMember(@PathVariable Long cardId, @PathVariable Long userId, Principal principal) {
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.assignMember(cardId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{cardId}/assignees/{userId}")
    public ResponseEntity<?> unassignMember(@PathVariable Long cardId, @PathVariable Long userId, Principal principal) {
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.unassignMember(cardId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{cardId}/attachments")
    public ResponseEntity<?> getCardAttachments(@PathVariable Long cardId, Principal principal) {
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }

        // Chuyển đổi từ Entity sang DTO
        List<AttachmentDto> attachmentDtos = card.getAttachments().stream()
                .map(att -> {
                    AttachmentDto dto = new AttachmentDto();
                    dto.setId(att.getId());
                    dto.setFileName(att.getFileName());
                    // Tạo URL để truy cập file
                    dto.setUrl("/attachments/" + att.getStoredFileName());
                    return dto;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(attachmentDtos, HttpStatus.OK);
    }

    @PostMapping("/{cardId}/labels/{labelId}")
    public ResponseEntity<?> assignLabel(@PathVariable Long cardId, @PathVariable Long labelId, Principal principal) {
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.assignLabel(cardId, labelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{cardId}/labels/{labelId}")
    public ResponseEntity<?> unassignLabel(@PathVariable Long cardId, @PathVariable Long labelId, Principal principal) {
        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) {
            return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        }
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }
        cardService.unassignLabel(cardId, labelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}