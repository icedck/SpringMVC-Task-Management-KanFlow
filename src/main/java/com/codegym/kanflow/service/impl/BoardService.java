package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.model.Label;
import com.codegym.kanflow.model.Attachment;
import com.codegym.kanflow.repository.BoardRepository;
import com.codegym.kanflow.repository.UserRepository;
import com.codegym.kanflow.service.IBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService implements IBoardService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Override
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Override
    public void save(Board board) {
        if (board.getId() == null) {
            User owner = board.getOwner();
            List<User> members = new ArrayList<>();
            members.add(owner);
            board.setMembers(members);
        }
        boardRepository.save(board);
    }

    @Override
    public Board findById(Long id) {
        return boardRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Board findByIdWithDetails(Long id) {
        Board board = boardRepository.findByIdWithMembersAndOwner(id).orElse(null);

        if (board != null) {
            // Force load all collections to prevent LazyInitializationException
            board.getCardLists().size();
            board.getMembers().size();
            board.getOwner().getUsername();

            for (CardList list : board.getCardLists()) {
                list.getCards().size();
                
                for (Card card : list.getCards()) {
                    // Force load all card collections
                    card.getAssignees().size();
                    card.getLabels().size();
                    card.getAttachments().size();
                    
                    // Force load assignees details
                    for (User assignee : card.getAssignees()) {
                        assignee.getUsername();
                        assignee.getEmail();
                    }
                    
                    // Force load labels details
                    for (Label label : card.getLabels()) {
                        label.getName();
                        label.getColor();
                    }
                    
                    // Force load attachments details
                    for (Attachment attachment : card.getAttachments()) {
                        attachment.getFileName();
                        attachment.getStoredFileName();
                        attachment.getFileType();
                        attachment.getUploadDate();
                    }
                }
            }
        }
        return board;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Board> findByUser(User user) {
        List<Board> boards = boardRepository.findBoardsByUser(user);
        for (Board board : boards) {
            board.getOwner().getUsername();
            board.getMembers().size();
        }
        return boards;
    }

    @Override
    @Transactional // Rất quan trọng! Transaction phải được bắt đầu từ đây.
    public void deleteById(Long id) {
        Board boardToDelete = boardRepository.findById(id).orElse(null);

        if (boardToDelete != null) {
            boardToDelete.getMembers().clear();
            boardRepository.delete(boardToDelete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Long boardId, String username) {
        Board board = findById(boardId);
        if (board == null) {
            return false;
        }

        for (User member : board.getMembers()) {
            if (member.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public String inviteMember(Long boardId, String email, String currentUsername) {
        Board board = findById(boardId);
        User userToInvite = userRepository.findByEmail(email);
        User currentUser = userRepository.findByUsername(currentUsername);

        if (board == null || userToInvite == null) {
            return "Board or user with this email not found.";
        }
        if (!board.getOwner().getId().equals(currentUser.getId())) {
            return "Only the board owner can invite members.";
        }
        if (board.getMembers().contains(userToInvite)) {
            return "User is already a member of this board.";
        }

        board.getMembers().add(userToInvite);

        return userToInvite.getUsername() + " has been added to the board.";
    }

    @Override
    public Board findByIdWithOwner(Long id) {
        return boardRepository.findByIdWithOwner(id).orElse(null);
    }

    @Override
    @Transactional
    public String removeMember(Long boardId, Long userIdToRemove, String currentUsername) {
        Board board = boardRepository.findById(boardId).orElse(null);
        if (board == null) {
            return "Board not found.";
        }

        User currentUser = userRepository.findByUsername(currentUsername);
        User userToRemove = userRepository.findById(userIdToRemove).orElse(null);

        if (userToRemove == null) {
            return "User to remove not found.";
        }

        if (!board.getOwner().getId().equals(currentUser.getId())) {
            return "Only the board owner can remove members.";
        }

        if (board.getOwner().getId().equals(userToRemove.getId())) {
            return "The owner cannot be removed from the board.";
        }

        if (!board.getMembers().contains(userToRemove)) {
            return "User is not a member of this board.";
        }

        for (CardList list : board.getCardLists()) {
            for (Card card : list.getCards()) {
                card.getAssignees().remove(userToRemove);
            }
        }

        board.getMembers().remove(userToRemove);

        return userToRemove.getUsername() + " has been removed from the board.";
    }
}