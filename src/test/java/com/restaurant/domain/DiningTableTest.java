package com.restaurant.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiningTableTest {

    @Test
    void newTable_isAvailableByDefault() {
        DiningTable table = new DiningTable(4, 4);

        assertThat(table.isAvailable()).isTrue();
    }

    @Test
    void markReserved_makesTableUnavailable() {
        DiningTable table = new DiningTable(4, 4);

        table.markReserved();

        assertThat(table.isAvailable()).isFalse();
    }

    @Test
    void updateSeats_changesSeatCount() {
        DiningTable table = new DiningTable(4, 4);

        table.updateSeats(6);

        assertThat(table.getSeats()).isEqualTo(6);
    }
}
