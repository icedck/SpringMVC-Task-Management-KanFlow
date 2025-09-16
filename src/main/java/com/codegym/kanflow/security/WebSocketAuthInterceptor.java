package com.codegym.kanflow.security;

import com.codegym.kanflow.service.IUserService;
import com.codegym.kanflow.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String username = accessor.getFirstNativeHeader("username");
                if (username != null) {
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        // Convert roles to authorities
                        Collection<GrantedAuthority> authorities = new ArrayList<>();
                        for (com.codegym.kanflow.model.Role role : user.getRoles()) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                        }
                        
                        // Create authentication token
                        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                        accessor.setUser(auth);
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't crash the WebSocket connection
            System.err.println("Error in WebSocketAuthInterceptor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return message;
    }
}
