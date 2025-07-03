package com.codegym.kanflow.service;

import com.codegym.kanflow.model.CardList;

public interface ICardListService {
    /**
     * Lưu một đối tượng CardList mới hoặc cập nhật một đối tượng đã có.
     * @param cardList Đối tượng CardList cần lưu.
     * @return Đối tượng CardList sau khi đã được lưu.
     */
    CardList save(CardList cardList);

    /**
     * Tìm một CardList theo ID.
     * @param id ID của CardList cần tìm.
     * @return Đối tượng CardList nếu tìm thấy, ngược lại trả về null.
     */
    CardList findById(Long id); // <-- THÊM DÒNG NÀY

    void deleteById(Long id);
}