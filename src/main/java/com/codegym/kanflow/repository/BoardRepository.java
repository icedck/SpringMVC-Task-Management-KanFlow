package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    /**
     * Tìm một Board theo ID và lấy tất cả các CardList và Card liên quan
     * trong cùng một câu truy vấn để tránh LazyInitializationException.
     *
     * @param id ID của Board cần tìm.
     * @return một Optional chứa Board với đầy đủ dữ liệu.
     */
    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.cardLists cl WHERE b.id = :id")
    Optional<Board> findByIdWithDetails(@Param("id") Long id);

    List<Board> findAllByOwner(User owner);
}