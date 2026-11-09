package com.campuscart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Restricts registration to the college email domain (set app.email-domain; "*" allows any). */
@Component
public class EmailPolicy {
    private final String domain;

    public EmailPolicy(@Value("${app.email-domain:thapar.edu}") String domain) {
        this.domain = domain.trim().toLowerCase();
    }

    public boolean isAllowed(String email) {
        if (email == null) return false;
        if ("*".equals(domain)) return true;
        String e = email.trim().toLowerCase();
        return e.endsWith("@" + domain) || e.endsWith("." + domain);
    }

    public String domain() { return domain; }
}
