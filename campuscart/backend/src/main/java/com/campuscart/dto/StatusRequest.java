package com.campuscart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

/** price is only used with the COUNTER action. */
public record StatusRequest(
        @NotBlank String action,
        @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal price) {}
