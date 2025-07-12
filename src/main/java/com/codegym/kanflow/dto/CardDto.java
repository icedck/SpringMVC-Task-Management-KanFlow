package com.codegym.kanflow.dto;

import java.util.Collections;
import java.util.List; // Thêm import này
import java.util.Set;

public class CardDto {
    private Long id;
    private String title;
    private String description;
    private int position;
    private List<UserDto> assignees;

    // === THÊM TRƯỜNG MỚI CHO LABELS ===
    private Set<LabelDto> labels;

    // Constructor mặc định
    public CardDto() {
        this.labels = Collections.emptySet(); // Khởi tạo để tránh null
    }

    // Constructor cũ của bạn (có thể giữ lại hoặc xóa đi nếu không dùng)
    public CardDto(Long id, String title, String description, int position, List<UserDto> assignees) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.position = position;
        this.assignees = assignees;
        this.labels = Collections.emptySet(); // Gán danh sách rỗng
    }

    // === TẠO MỘT CONSTRUCTOR ĐẦY ĐỦ MỚI ===
    public CardDto(Long id, String title, String description, int position, List<UserDto> assignees, Set<LabelDto> labels) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.position = position;
        this.assignees = assignees;
        this.labels = labels;
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

    public Set<LabelDto> getLabels() { return labels; }
    public void setLabels(Set<LabelDto> labels) { this.labels = labels; }
}