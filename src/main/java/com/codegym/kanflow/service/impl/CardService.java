package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.repository.CardRepository;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardService implements ICardService {
    @Autowired
    private CardRepository cardRepository;

    @Override
    public Card save(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public Card findById(Long id) {
        return cardRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        cardRepository.deleteById(id);
    }
}