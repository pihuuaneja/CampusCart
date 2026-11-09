package com.campuscart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.campuscart.exception.ApiException;
import com.campuscart.model.ConversationView;
import com.campuscart.model.ListingView;
import com.campuscart.model.MessageView;
import com.campuscart.repository.ChatRepository;
import com.campuscart.repository.ListingRepository;

/** One conversation per (listing, buyer). The seller is the listing owner. */
@Service
public class ChatService {
    private final ChatRepository chats;
    private final ListingRepository listings;

    public ChatService(ChatRepository chats, ListingRepository listings) {
        this.chats = chats;
        this.listings = listings;
    }

    public ConversationView start(long userId, long listingId) {
        ListingView l = listings.findById(listingId).orElseThrow(() -> ApiException.notFound("Listing"));
        if (l.sellerId() == userId) throw ApiException.badRequest("This is your own listing");
        long id = chats.findConversation(listingId, userId)
                .orElseGet(() -> chats.createConversation(listingId, userId));
        return chats.listForUser(userId).stream().filter(c -> c.id() == id).findFirst().orElseThrow();
    }

    public List<ConversationView> mine(long userId) { return chats.listForUser(userId); }

    public List<MessageView> messages(long userId, long conversationId, long afterId) {
        requireParticipant(userId, conversationId);
        return chats.messages(conversationId, afterId);
    }

    public MessageView send(long userId, long conversationId, String body) {
        requireParticipant(userId, conversationId);
        String text = body == null ? "" : body.trim();
        if (text.isEmpty()) throw ApiException.badRequest("Write a message first");
        return chats.addMessage(conversationId, userId, text);
    }

    private void requireParticipant(long userId, long conversationId) {
        ChatRepository.Participants p = chats.findParticipants(conversationId)
                .orElseThrow(() -> ApiException.notFound("Conversation"));
        if (p.buyerId() != userId && p.sellerId() != userId) {
            throw ApiException.forbidden("You are not part of this conversation");
        }
    }
}
