package com.restaurant.controller;

import com.restaurant.controller.dto.MenuItemRequest;
import com.restaurant.controller.dto.MenuItemResponse;
import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import com.restaurant.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@Tag(name = "Menu items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a menu item")
    public MenuItemResponse create(@Valid @RequestBody MenuItemRequest request) {
        MenuItem created = menuItemService.create(
                request.name(), request.category(), request.price(), request.preparationMinutes());
        return MenuItemResponse.from(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a menu item by id")
    public MenuItemResponse findById(@PathVariable Long id) {
        return MenuItemResponse.from(menuItemService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List available menu items in a category")
    public List<MenuItemResponse> findAvailableByCategory(@RequestParam MenuItemCategory category) {
        return menuItemService.findAvailableByCategory(category).stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a menu item's name, price, and preparation time")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        MenuItem updated = menuItemService.updateDetails(
                id, request.name(), request.price(), request.preparationMinutes());
        return MenuItemResponse.from(updated);
    }

    @PatchMapping("/{id}/unavailable")
    @Operation(summary = "Take a menu item off the menu")
    public MenuItemResponse markUnavailable(@PathVariable Long id) {
        return MenuItemResponse.from(menuItemService.markUnavailable(id));
    }

    @PatchMapping("/{id}/available")
    @Operation(summary = "Put a menu item back on the menu")
    public MenuItemResponse markAvailable(@PathVariable Long id) {
        return MenuItemResponse.from(menuItemService.markAvailable(id));
    }
}
