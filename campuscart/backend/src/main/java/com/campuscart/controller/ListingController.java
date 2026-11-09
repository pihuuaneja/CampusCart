package com.campuscart.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.campuscart.dto.ListingFilter;
import com.campuscart.dto.ListingRequest;
import com.campuscart.model.ListingView;
import com.campuscart.service.ListingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private final ListingService service;

    public ListingController(ListingService service) { this.service = service; }

    @GetMapping
    public List<ListingView> search(@RequestParam(required = false) String q,
                                    @RequestParam(required = false) Integer categoryId,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) Boolean hostelQuick,
                                    @RequestParam(required = false) BigDecimal minPrice,
                                    @RequestParam(required = false) BigDecimal maxPrice,
                                    @RequestParam(required = false) String sort) {
        return service.search(new ListingFilter(q, categoryId, type, hostelQuick, minPrice, maxPrice, sort));
    }

    @GetMapping("/mine")
    public List<ListingView> mine(@AuthenticationPrincipal Long userId) { return service.mine(userId); }

    @GetMapping("/{id}")
    public ListingView get(@PathVariable long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingView create(@AuthenticationPrincipal Long userId, @Valid @RequestBody ListingRequest r) {
        return service.create(userId, r);
    }

    @PutMapping("/{id}")
    public ListingView update(@AuthenticationPrincipal Long userId, @PathVariable long id,
                              @Valid @RequestBody ListingRequest r) {
        return service.update(userId, id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable long id) {
        service.delete(userId, id);
    }
}
