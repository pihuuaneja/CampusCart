package com.campuscart.model;

public record UserView(Long id, String name, String email, String hostel) {
    public static UserView from(User u) {
        return new UserView(u.id(), u.name(), u.email(), u.hostel());
    }
}
