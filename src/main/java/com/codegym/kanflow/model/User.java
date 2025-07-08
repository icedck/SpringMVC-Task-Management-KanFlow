package com.codegym.kanflow.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 3, message = "Password must have at least 3 characters")
    private String password;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    // Quan hệ 1-Nhiều: Một User có thể sở hữu nhiều Board.
    // 'mappedBy = "owner"' chỉ ra rằng mối quan hệ này được quản lý bởi trường 'owner' trong lớp Board.
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Board> boards;

    @ManyToMany(mappedBy = "members")
    private List<Board> sharedBoards;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }

    public List<Board> getSharedBoards() {
        return sharedBoards;
    }

    public void setSharedBoards(List<Board> sharedBoards) {
        this.sharedBoards = sharedBoards;
    }
}