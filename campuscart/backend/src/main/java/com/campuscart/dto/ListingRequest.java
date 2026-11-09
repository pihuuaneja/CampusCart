package com.campuscart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ListingRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull Integer categoryId,
        @NotBlank @Pattern(regexp = "SELL|RENT", message = "must be SELL or RENT") String type,
        @Pattern(regexp = "DAY|WEEK|MONTH", message = "must be DAY, WEEK or MONTH") String rentPeriod,
        boolean hostelQuick,
        @Size(max = 500) String imageUrl) {}
