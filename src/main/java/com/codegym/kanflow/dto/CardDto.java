package com.codegym.kanflow.dto;

import java.util.Collections;
import java.util.List; // Thêm import này

public class CardDto {
    private Long id;
    private String title;
    private String description;
    private int position;

    // 1. Thêm trường mới để chứa danh sách người được gán
    private List<UserDto> assignees;

    // Constructor mặc định
    public CardDto() {
        // Khởi tạo danh sách rỗng để tránh NullPointerException
        this.assignees = Collections.emptyList();
    }

    // Constructor cũ của bạn (chỉ cần cho việc tạo card mới từ frontend)
    public CardDto(Long id, String title, String description, int position) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.position = position;
        this.assignees = Collections.emptyList(); // Gán danh sách rỗng
    }

    // 2. Tạo một Constructor đầy đủ mới để dùng trong Controller
    public CardDto(Long id, String title, String description, int position, List<UserDto> assignees) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.position = position;
        this.assignees = assignees;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    // 3. Thêm Getter và Setter cho trường mới
    public List<UserDto> getAssignees() { return assignees; }
    public void setAssignees(List<UserDto> assignees) { this.assignees = assignees; }
}