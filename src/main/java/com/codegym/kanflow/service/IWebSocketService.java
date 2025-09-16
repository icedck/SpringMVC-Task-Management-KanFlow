package com.codegym.kanflow.service;

import com.codegym.kanflow.dto.WebSocketMessage;

public interface IWebSocketService {
    void sendToBoard(Long boardId, WebSocketMessage message);
    void sendToUser(String username, String destination, Object message);
    void broadcastToBoard(Long boardId, WebSocketMessage message);
}

