package com.restaurant.controller.dto;

import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        MenuItemCategory category,
        BigDecimal price,
        int preparationMinutes,
        boolean available) {

    public static MenuItemResponse from(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getCategory(),
                menuItem.getPrice(),
                menuItem.getPreparationMinutes(),
                menuItem.isAvailable());
    }
}
