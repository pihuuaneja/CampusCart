package com.campuscart.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ListingView(
        Long id, Long sellerId, String sellerName, String sellerHostel,
        Integer categoryId, String categoryName,
        String title, String description, BigDecimal price,
        String type, String rentPeriod, boolean hostelQuick,
        String imageUrl, String status, Instant createdAt) {}
