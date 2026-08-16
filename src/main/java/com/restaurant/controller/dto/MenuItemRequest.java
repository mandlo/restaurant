package com.restaurant.controller.dto;

import com.restaurant.domain.MenuItemCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank String name,
        @NotNull MenuItemCategory category,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Positive int preparationMinutes) {
}
