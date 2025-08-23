package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
//    List<Card> findByCardListOrderByPositionAsc(CardList cardList);

    @Query("SELECT DISTINCT c FROM Card c " +
            "LEFT JOIN FETCH c.cardList cl " +
            "LEFT JOIN FETCH cl.board " +
            "LEFT JOIN FETCH c.assignees " +
            "LEFT JOIN FETCH c.attachments " +
            "LEFT JOIN FETCH c.labels " +
            "WHERE c.id = :id")
    Optional<Card> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM Card c WHERE :user MEMBER OF c.assignees")
    List<Card> findAllByAssignee(@Param("user") User user);
}
