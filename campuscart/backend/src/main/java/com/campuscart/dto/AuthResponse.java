package com.campuscart.dto;

import com.campuscart.model.UserView;

public record AuthResponse(String token, UserView user) {}
