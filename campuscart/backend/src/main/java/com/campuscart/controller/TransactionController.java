package com.campuscart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.campuscart.dto.StatusRequest;
import com.campuscart.dto.TransactionRequest;
import com.campuscart.model.TransactionView;
import com.campuscart.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionView request(@AuthenticationPrincipal Long userId, @Valid @RequestBody TransactionRequest r) {
        return service.request(userId, r.listingId(), r.message(), r.offerPrice());
    }

    @GetMapping("/bought")
    public List<TransactionView> bought(@AuthenticationPrincipal Long userId) { return service.bought(userId); }

    @GetMapping("/received")
    public List<TransactionView> received(@AuthenticationPrincipal Long userId) { return service.received(userId); }

    @PatchMapping("/{id}/status")
    public TransactionView updateStatus(@AuthenticationPrincipal Long userId, @PathVariable long id,
                                        @Valid @RequestBody StatusRequest r) {
        return service.updateStatus(userId, id, r.action(), r.price());
    }
}
