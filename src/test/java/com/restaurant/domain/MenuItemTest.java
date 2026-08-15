package com.restaurant.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemTest {

    @Test
    void newMenuItem_isAvailableByDefault() {
        MenuItem item = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);

        assertThat(item.isAvailable()).isTrue();
    }

    @Test
    void markUnavailable_changesAvailability() {
        MenuItem item = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);

        item.markUnavailable();

        assertThat(item.isAvailable()).isFalse();
    }

    @Test
    void markAvailable_reopensMenuItem() {
        MenuItem item = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);
        item.markUnavailable();

        item.markAvailable();

        assertThat(item.isAvailable()).isTrue();
    }

    @Test
    void updateDetails_changesNamePriceAndPreparationMinutes() {
        MenuItem item = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);

        item.updateDetails("Truffle Pasta", new BigDecimal("18.00"), 25);

        assertThat(item.getName()).isEqualTo("Truffle Pasta");
        assertThat(item.getPrice()).isEqualByComparingTo("18.00");
        assertThat(item.getPreparationMinutes()).isEqualTo(25);
    }
}
