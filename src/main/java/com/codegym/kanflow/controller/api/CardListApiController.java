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

@RestController
@RequestMapping("/api/lists")
public class CardListApiController {

    @Autowired
    private ICardListService cardListService; // Bây giờ có thể tiêm thành công

    @Autowired
    private IBoardService boardService;

    @PostMapping
    public ResponseEntity<CardListDto> createList(@RequestBody CardListDto cardListDto, @RequestParam Long boardId) {
        Board board = boardService.findById(boardId);
        if (board == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CardList listToSave = new CardList();
        listToSave.setTitle(cardListDto.getTitle());
        listToSave.setBoard(board);

        // Gọi service và nhận lại đối tượng đã được lưu
        CardList savedList = cardListService.save(listToSave);

        // Chuyển từ Entity đã lưu sang DTO để trả về
        CardListDto responseDto = new CardListDto(savedList.getId(), savedList.getTitle(), savedList.getPosition());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}