package com.campuscart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** offerPrice is optional. Leave it out to pay the asking price. */
public record TransactionRequest(
        @NotNull Long listingId,
        @Size(max = 300) String message,
        @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal offerPrice) {}
