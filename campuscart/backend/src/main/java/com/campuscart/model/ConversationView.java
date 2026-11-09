package com.campuscart.model;

import java.time.Instant;

public record ConversationView(
        Long id, Long listingId, String listingTitle, String listingImageUrl,
        Long otherUserId, String otherUserName, String lastMessage, Instant lastMessageAt) {}
