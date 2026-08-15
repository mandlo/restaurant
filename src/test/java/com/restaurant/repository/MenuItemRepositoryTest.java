package com.restaurant.repository;

import com.restaurant.AbstractIntegrationTest;
import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    void findByCategoryAndAvailableTrue_returnsOnlyAvailableItemsInCategory() {
        menuItemRepository.save(new MenuItem("Soup", MenuItemCategory.STARTER, new BigDecimal("6.00"), 10));
        MenuItem unavailable = menuItemRepository.save(
                new MenuItem("Salad", MenuItemCategory.STARTER, new BigDecimal("8.00"), 8));
        unavailable.markUnavailable();
        menuItemRepository.save(new MenuItem("Cake", MenuItemCategory.DESSERT, new BigDecimal("7.00"), 12));

        var results = menuItemRepository.findByCategoryAndAvailableTrue(MenuItemCategory.STARTER);

        assertThat(results).extracting(MenuItem::getName).containsExactly("Soup");
    }
}
