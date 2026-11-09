package com.campuscart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.campuscart.security.JwtService;

class JwtServiceTest {
    private final JwtService jwt = new JwtService("test-secret-test-secret-test-secret-123", 1);

    @Test
    void roundTripReturnsSameUserId() {
        assertEquals(42L, jwt.parseUserId(jwt.generate(42L)).orElseThrow());
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwt.generate(7L);
        assertTrue(jwt.parseUserId(token + "x").isEmpty());
    }

    @Test
    void garbageTokenIsRejected() {
        assertTrue(jwt.parseUserId("not-a-token").isEmpty());
    }
}
