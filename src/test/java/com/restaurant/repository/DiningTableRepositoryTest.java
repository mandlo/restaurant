package com.restaurant.repository;

import com.restaurant.AbstractIntegrationTest;
import com.restaurant.domain.DiningTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class DiningTableRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private DiningTableRepository diningTableRepository;

    @Test
    void findBySeatsGreaterThanEqualAndAvailableTrue_returnsTablesWithEnoughSeats() {
        diningTableRepository.save(new DiningTable(1, 2));
        diningTableRepository.save(new DiningTable(2, 6));
        DiningTable reserved = diningTableRepository.save(new DiningTable(3, 8));
        reserved.markReserved();

        var results = diningTableRepository.findBySeatsGreaterThanEqualAndAvailableTrue(4);

        assertThat(results).extracting(DiningTable::getTableNumber).containsExactly(2);
    }
}
