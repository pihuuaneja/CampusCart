package com.campuscart.model;

import java.math.BigDecimal;
import java.time.Instant;

/** price = the seller's asking price, offerPrice = the latest offer on the table. */
public record TransactionView(
        Long id, Long listingId, String listingTitle, BigDecimal price, BigDecimal offerPrice, String listingType,
        Long buyerId, String buyerName, Long sellerId, String sellerName,
        String status, String message, Instant createdAt, Instant updatedAt) {}
