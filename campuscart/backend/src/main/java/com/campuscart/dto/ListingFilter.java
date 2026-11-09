package com.campuscart.dto;

import java.math.BigDecimal;

public record ListingFilter(String q, Integer categoryId, String type, Boolean hostelQuick,
                            BigDecimal minPrice, BigDecimal maxPrice, String sort) {}
