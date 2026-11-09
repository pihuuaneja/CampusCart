package com.campuscart.model;

import java.time.Instant;

public record MessageView(Long id, Long conversationId, Long senderId, String senderName, String body, Instant createdAt) {}
