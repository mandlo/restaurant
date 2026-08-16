package com.restaurant.controller.dto;

import jakarta.validation.constraints.Positive;

public record UpdateSeatsRequest(@Positive int seats) {
}
