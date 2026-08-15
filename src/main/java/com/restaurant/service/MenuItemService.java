package com.restaurant.service;

import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import com.restaurant.repository.MenuItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> findAvailableByCategory(MenuItemCategory category) {
        return menuItemRepository.findByCategoryAndAvailableTrue(category);
    }

    @Transactional
    public MenuItem updateDetails(Long id, String name, BigDecimal price, int preparationMinutes) {
        MenuItem menuItem = findOrThrow(id);
        menuItem.updateDetails(name, price, preparationMinutes);
        return menuItem;
    }

    @Transactional
    public void markUnavailable(Long id) {
        findOrThrow(id).markUnavailable();
    }

    @Transactional
    public void markAvailable(Long id) {
        findOrThrow(id).markAvailable();
    }

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Menu item " + id + " not found"));
    }
}
