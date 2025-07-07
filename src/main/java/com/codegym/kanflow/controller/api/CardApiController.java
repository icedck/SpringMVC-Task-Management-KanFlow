package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.CardDto;
import com.codegym.kanflow.dto.CardMoveDto;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.service.IBoardService; // Thêm import
import com.codegym.kanflow.service.ICardListService;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Thêm import
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardApiController {

    @Autowired
    private ICardService cardService;

    @Autowired
    private ICardListService cardListService;

    @Autowired
    private IBoardService boardService; // Thêm BoardService

    @PostMapping
    public ResponseEntity<CardDto> createCard(@RequestBody CardDto cardDto, @RequestParam Long listId, Principal principal) {
        CardList cardList = cardListService.findById(listId);
        if (cardList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // --- KIỂM TRA QUYỀN TRUY CẬP ---
        if (!boardService.hasAccess(cardList.getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Card newCard = new Card();
        newCard.setTitle(cardDto.getTitle());
        newCard.setCardList(cardList);

        Card savedCard = cardService.save(newCard);
        CardDto responseDto = new CardDto(savedCard.getId(), savedCard.getTitle(), savedCard.getDescription(), savedCard.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardDetails(@PathVariable Long id, Principal principal) {
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // --- KIỂM TRA QUYỀN TRUY CẬP ---
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        CardDto responseDto = new CardDto(card.getId(), card.getTitle(), card.getDescription(), card.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id, @RequestBody CardDto cardDto, Principal principal) {
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // --- KIỂM TRA QUYỀN TRUY CẬP ---
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        card.setTitle(cardDto.getTitle());
        card.setDescription(cardDto.getDescription());
        Card updatedCard = cardService.save(card);
        CardDto responseDto = new CardDto(updatedCard.getId(), updatedCard.getTitle(), updatedCard.getDescription(), updatedCard.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id, Principal principal) { // Sửa kiểu trả về
        Card card = cardService.findByIdWithDetails(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // --- KIỂM TRA QUYỀN TRUY CẬP ---
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

        // --- KIỂM TRA QUYỀN TRUY CẬP ---
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Cũng nên kiểm tra quyền truy cập vào board đích
        if (!boardService.hasAccess(cardMoveDto.getTargetListId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            cardService.move(cardId, cardMoveDto.getTargetListId(), cardMoveDto.getNewPosition());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}