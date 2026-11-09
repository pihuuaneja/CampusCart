package com.campuscart.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.campuscart.dto.AuthResponse;
import com.campuscart.dto.LoginRequest;
import com.campuscart.dto.RegisterRequest;
import com.campuscart.exception.ApiException;
import com.campuscart.model.User;
import com.campuscart.model.UserView;
import com.campuscart.repository.UserRepository;
import com.campuscart.security.JwtService;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailPolicy emailPolicy;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, EmailPolicy emailPolicy) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.emailPolicy = emailPolicy;
    }

    public AuthResponse register(RegisterRequest r) {
        if (!emailPolicy.isAllowed(r.email())) {
            throw ApiException.badRequest("Use your college email address (@" + emailPolicy.domain() + ")");
        }
        if (users.findByEmail(r.email()).isPresent()) {
            throw ApiException.conflict("An account with this email already exists");
        }
        long id = users.create(r.name().trim(), r.email().trim(), encoder.encode(r.password()), r.hostel());
        User saved = users.findById(id).orElseThrow();
        return new AuthResponse(jwt.generate(id), UserView.from(saved));
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim())
                .filter(x -> encoder.matches(r.password(), x.passwordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect email or password"));
        return new AuthResponse(jwt.generate(u.id()), UserView.from(u));
    }

    public UserView me(long userId) {
        return users.findById(userId).map(UserView::from)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }
}
