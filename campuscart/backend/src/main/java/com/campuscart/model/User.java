package com.campuscart.model;

public record User(Long id, String name, String email, String passwordHash, String hostel) {}
