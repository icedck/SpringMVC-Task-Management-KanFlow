package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Card;

public interface ICardService {
    Card save(Card card);

    Card findById(Long id);

    void deleteById(Long id);
}