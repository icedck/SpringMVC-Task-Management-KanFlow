package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.dto.WebSocketMessage;
import com.codegym.kanflow.service.IWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements IWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToBoard(Long boardId, WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
    }

    @Override
    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    @Override
    public void broadcastToBoard(Long boardId, WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
    }
}

