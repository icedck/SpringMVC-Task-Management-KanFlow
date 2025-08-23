package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.repository.CardListRepository;
import com.codegym.kanflow.service.ICardListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardListService implements ICardListService {

    @Autowired
    private CardListRepository cardListRepository;

    @Override
    public CardList save(CardList cardList) {
        return cardListRepository.save(cardList);
    }

    @Override
    public CardList findByIdWithBoard(Long id) {
        return cardListRepository.findByIdWithBoard(id).orElse(null);
    }

    @Override
    public CardList findById(Long id) {
        return cardListRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        cardListRepository.deleteById(id);
    }

//    @Override
//    public void updatePositions(List<Long> listIds) {
//        for (int i = 0; i < listIds.size(); i++) {
//            Long listId = listIds.get(i);
//            int newPosition = i;
//
//            // Dùng orElseThrow để xử lý trường hợp không tìm thấy list
//            CardList list = cardListRepository.findByIdWithBoard(listId)
//                    .orElseThrow(() -> new EntityNotFoundException("CardList not found with id: " + listId));
//
//            list.setPosition(newPosition);
//
//            // **SỬA LỖI:** Thêm lời gọi save() tường minh
//            cardListRepository.save(list);
//        }
//    }

    @Override
    public void updatePositions(List<Long> listIds) {
        for (int i = 0; i < listIds.size(); i++) {
            Long listId = listIds.get(i);
            int newPosition = i;

            cardListRepository.findById(listId).ifPresent(list -> {
                list.setPosition(newPosition);
                cardListRepository.save(list);
            });
        }
    }
}