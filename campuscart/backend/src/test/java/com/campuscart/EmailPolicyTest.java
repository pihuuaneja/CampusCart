package com.campuscart;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.campuscart.service.EmailPolicy;

class EmailPolicyTest {
    @Test
    void acceptsCollegeDomainAndSubdomains() {
        EmailPolicy p = new EmailPolicy("thapar.edu");
        assertTrue(p.isAllowed("asha@thapar.edu"));
        assertTrue(p.isAllowed("ASHA@CS.thapar.edu"));
    }

    @Test
    void rejectsOtherDomains() {
        EmailPolicy p = new EmailPolicy("thapar.edu");
        assertFalse(p.isAllowed("asha@gmail.com"));
        assertFalse(p.isAllowed("asha@notthapar.edu"));
    }

    @Test
    void wildcardAllowsAnything() {
        assertTrue(new EmailPolicy("*").isAllowed("anyone@example.com"));
    }
}
