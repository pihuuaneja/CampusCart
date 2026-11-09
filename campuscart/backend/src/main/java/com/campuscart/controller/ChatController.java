package com.campuscart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.campuscart.dto.MessageRequest;
import com.campuscart.dto.StartChatRequest;
import com.campuscart.model.ConversationView;
import com.campuscart.model.MessageView;
import com.campuscart.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService service;

    public ChatController(ChatService service) { this.service = service; }

    /** Buyer opens (or reopens) the conversation about a listing. */
    @PostMapping
    public ConversationView start(@AuthenticationPrincipal Long userId, @Valid @RequestBody StartChatRequest r) {
        return service.start(userId, r.listingId());
    }

    @GetMapping
    public List<ConversationView> mine(@AuthenticationPrincipal Long userId) { return service.mine(userId); }

    /** Pass after=<last message id> to receive only newer messages. */
    @GetMapping("/{id}/messages")
    public List<MessageView> messages(@AuthenticationPrincipal Long userId, @PathVariable long id,
                                      @RequestParam(defaultValue = "0") long after) {
        return service.messages(userId, id, after);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageView send(@AuthenticationPrincipal Long userId, @PathVariable long id,
                            @Valid @RequestBody MessageRequest r) {
        return service.send(userId, id, r.body());
    }
}
