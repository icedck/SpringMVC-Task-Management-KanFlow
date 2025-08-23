package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.CardListDto;
import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.ICardListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Thêm import
import java.util.List;

@RestController
@RequestMapping("/api/lists")
public class CardListApiController {

    @Autowired
    private ICardListService cardListService;

    @Autowired
    private IBoardService boardService;

    @PostMapping
    public ResponseEntity<CardListDto> createList(@RequestBody CardListDto cardListDto, @RequestParam Long boardId, Principal principal) {
        if (!boardService.hasAccess(boardId, principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Board board = boardService.findById(boardId);
        if (board == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CardList listToSave = new CardList();
        listToSave.setTitle(cardListDto.getTitle());
        listToSave.setBoard(board);

        CardList savedList = cardListService.save(listToSave);
        CardListDto responseDto = new CardListDto(savedList.getId(), savedList.getTitle(), savedList.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardListDto> updateListTitle(@PathVariable Long id, @RequestBody CardListDto cardListDto, Principal principal) {
        CardList listToUpdate = cardListService.findByIdWithBoard(id);
        if (listToUpdate == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!boardService.hasAccess(listToUpdate.getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        listToUpdate.setTitle(cardListDto.getTitle());
        CardList savedList = cardListService.save(listToUpdate);
        CardListDto responseDto = new CardListDto(savedList.getId(), savedList.getTitle(), savedList.getPosition());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id, Principal principal) {
        CardList listToDelete = cardListService.findByIdWithBoard(id);
        if (listToDelete == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!boardService.hasAccess(listToDelete.getBoard().getId(), principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        cardListService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/updatePositions")
    public ResponseEntity<Void> updateListPositions(@RequestBody List<Long> listIds, Principal principal) {
        if (listIds != null && !listIds.isEmpty()) {
            CardList firstList = cardListService.findByIdWithBoard(listIds.get(0));
            if (firstList != null) {
                if (!boardService.hasAccess(firstList.getBoard().getId(), principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }

        cardListService.updatePositions(listIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}