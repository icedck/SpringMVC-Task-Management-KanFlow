package com.codegym.kanflow.service;

import com.codegym.kanflow.model.CardList;

public interface ICardListService {
    /**
     * Lưu một đối tượng CardList mới hoặc cập nhật một đối tượng đã có.
     * @param cardList Đối tượng CardList cần lưu.
     * @return Đối tượng CardList sau khi đã được lưu (có thể chứa ID được tạo mới).
     */
    CardList save(CardList cardList);

    // Chúng ta sẽ thêm các phương thức khác như findById, delete sau khi cần đến
}