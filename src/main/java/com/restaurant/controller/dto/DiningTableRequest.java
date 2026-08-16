package com.restaurant.controller.dto;

import jakarta.validation.constraints.Positive;

public record DiningTableRequest(
        @Positive int tableNumber,
        @Positive int seats) {
}
