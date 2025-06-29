package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.Card;
import org.springframework.data.repository.CrudRepository;

public interface CardRepository extends CrudRepository<Card, Long> {
}
