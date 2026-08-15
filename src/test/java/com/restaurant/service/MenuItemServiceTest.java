package com.restaurant.service;

import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import com.restaurant.repository.MenuItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    private MenuItemService menuItemService;

    @BeforeEach
    void setUp() {
        menuItemService = new MenuItemService(menuItemRepository);
    }

    @Test
    void findAvailableByCategory_delegatesToRepository() {
        MenuItem soup = new MenuItem("Soup", MenuItemCategory.STARTER, new BigDecimal("6.00"), 10);
        when(menuItemRepository.findByCategoryAndAvailableTrue(MenuItemCategory.STARTER))
                .thenReturn(List.of(soup));

        List<MenuItem> result = menuItemService.findAvailableByCategory(MenuItemCategory.STARTER);

        assertThat(result).containsExactly(soup);
    }

    @Test
    void updateDetails_updatesAnExistingMenuItem() {
        MenuItem pasta = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(pasta));

        MenuItem updated = menuItemService.updateDetails(1L, "Truffle Pasta", new BigDecimal("18.00"), 25);

        assertThat(updated.getName()).isEqualTo("Truffle Pasta");
        assertThat(updated.getPrice()).isEqualByComparingTo("18.00");
        assertThat(updated.getPreparationMinutes()).isEqualTo(25);
    }

    @Test
    void updateDetails_throwsWhenMenuItemDoesNotExist() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuItemService.updateDetails(1L, "Truffle Pasta", new BigDecimal("18.00"), 25))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void markUnavailable_marksAnExistingMenuItemUnavailable() {
        MenuItem pasta = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(pasta));

        menuItemService.markUnavailable(1L);

        assertThat(pasta.isAvailable()).isFalse();
    }

    @Test
    void markUnavailable_throwsWhenMenuItemDoesNotExist() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuItemService.markUnavailable(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void markAvailable_marksAnExistingMenuItemAvailable() {
        MenuItem pasta = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);
        pasta.markUnavailable();
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(pasta));

        menuItemService.markAvailable(1L);

        assertThat(pasta.isAvailable()).isTrue();
    }

    @Test
    void markAvailable_throwsWhenMenuItemDoesNotExist() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuItemService.markAvailable(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
