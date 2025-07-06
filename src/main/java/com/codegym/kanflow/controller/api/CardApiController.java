package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.CardDto;
import com.codegym.kanflow.dto.CardMoveDto;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.service.ICardListService;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Thêm service findById cho CardList
// 1. Vào ICardListService thêm CardList findById(Long id);
// 2. Vào CardListService triển khai:
// @Override public CardList findById(Long id) { return cardListRepository.findById(id).orElse(null); }

@RestController
@RequestMapping("/api/cards")
public class CardApiController {

    @Autowired
    private ICardService cardService;

    @Autowired
    private ICardListService cardListService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@RequestBody CardDto cardDto, @RequestParam Long listId) {
        // Tìm list mà card này thuộc về
        CardList cardList = cardListService.findById(listId);
        if (cardList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Chuyển từ DTO sang Entity để lưu
        Card newCard = new Card();
        newCard.setTitle(cardDto.getTitle());
        newCard.setCardList(cardList);
        // Tạm thời chưa xử lý position và description

        Card savedCard = cardService.save(newCard);

        // Chuyển từ Entity đã lưu sang DTO để trả về
        CardDto responseDto = new CardDto(savedCard.getId(), savedCard.getTitle(), savedCard.getDescription(), savedCard.getPosition());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardDetails(@PathVariable Long id) {
        Card card = cardService.findById(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CardDto responseDto = new CardDto(card.getId(), card.getTitle(), card.getDescription(), card.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id, @RequestBody CardDto cardDto) {
        Card card = cardService.findById(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        card.setTitle(cardDto.getTitle());
        card.setDescription(cardDto.getDescription());

        Card updatedCard = cardService.save(card);

        CardDto responseDto = new CardDto(card.getId(), card.getTitle(), card.getDescription(), card.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CardDto> deleteCard(@PathVariable Long id) {
        Card card = cardService.findById(id);
        if (card == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        cardService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/updatePositions")
    public ResponseEntity<Void> updateCardPositions(@RequestBody List<Long> cardIds) {
        cardService.updatePositions(cardIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{cardId}/move")
    public ResponseEntity<Void> moveCard(@PathVariable Long cardId, @RequestBody CardMoveDto cardMoveDto) {
        try {
            cardService.move(cardId, cardMoveDto.getTargetListId(), cardMoveDto.getNewPosition());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}