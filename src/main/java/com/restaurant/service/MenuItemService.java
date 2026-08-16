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

    @Transactional
    public MenuItem create(String name, MenuItemCategory category, BigDecimal price, int preparationMinutes) {
        return menuItemRepository.save(new MenuItem(name, category, price, preparationMinutes));
    }

    @Transactional(readOnly = true)
    public MenuItem findById(Long id) {
        return findOrThrow(id);
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
    public MenuItem markUnavailable(Long id) {
        MenuItem menuItem = findOrThrow(id);
        menuItem.markUnavailable();
        return menuItem;
    }

    @Transactional
    public MenuItem markAvailable(Long id) {
        MenuItem menuItem = findOrThrow(id);
        menuItem.markAvailable();
        return menuItem;
    }

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Menu item " + id + " not found"));
    }
}
