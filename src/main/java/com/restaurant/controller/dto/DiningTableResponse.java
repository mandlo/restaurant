package com.restaurant.controller.dto;

import com.restaurant.domain.DiningTable;

public record DiningTableResponse(Long id, int tableNumber, int seats, boolean available) {

    public static DiningTableResponse from(DiningTable table) {
        return new DiningTableResponse(table.getId(), table.getTableNumber(), table.getSeats(), table.isAvailable());
    }
}
