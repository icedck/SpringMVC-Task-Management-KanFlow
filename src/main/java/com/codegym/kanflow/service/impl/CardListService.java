package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.repository.CardListRepository;
import com.codegym.kanflow.service.ICardListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // Đánh dấu đây là một Spring Bean thuộc tầng Service
public class CardListService implements ICardListService { // Triển khai interface

    @Autowired
    private CardListRepository cardListRepository; // Tiêm Repository tương ứng

    @Override
    public CardList save(CardList cardList) {
        return cardListRepository.save(cardList);
    }

    @Override
    public CardList findById(Long id) {
        return cardListRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        cardListRepository.deleteById(id);
    }
}