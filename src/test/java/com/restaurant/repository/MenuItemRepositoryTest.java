package com.restaurant.repository;

import com.restaurant.AbstractIntegrationTest;
import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    @Transactional
    void editingManagedEntity_flushesChangesWithoutExplicitSave() {
        MenuItem saved = menuItemRepository.save(
                new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20));
        Long id = saved.getId();

        MenuItem managed = menuItemRepository.findById(id).orElseThrow();
        managed.updateDetails("Truffle Pasta", new BigDecimal("18.00"), 25);

        entityManager.flush();
        entityManager.clear();

        MenuItem reloaded = menuItemRepository.findById(id).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Truffle Pasta");
        assertThat(reloaded.getPrice()).isEqualByComparingTo("18.00");
        assertThat(reloaded.getPreparationMinutes()).isEqualTo(25);
    }
}
