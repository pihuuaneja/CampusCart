package com.campuscart.dto;

import jakarta.validation.constraints.NotNull;

public record StartChatRequest(@NotNull Long listingId) {}
