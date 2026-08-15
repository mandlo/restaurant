package com.restaurant.service;

import com.restaurant.domain.DiningTable;
import com.restaurant.repository.DiningTableRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiningTableServiceTest {

    @Mock
    private DiningTableRepository diningTableRepository;

    private DiningTableService diningTableService;

    @BeforeEach
    void setUp() {
        diningTableService = new DiningTableService(diningTableRepository);
    }

    @Test
    void findAvailableWithMinimumSeats_delegatesToRepository() {
        DiningTable table = new DiningTable(2, 6);
        when(diningTableRepository.findBySeatsGreaterThanEqualAndAvailableTrue(4))
                .thenReturn(List.of(table));

        List<DiningTable> result = diningTableService.findAvailableWithMinimumSeats(4);

        assertThat(result).containsExactly(table);
    }

    @Test
    void reserve_marksAnExistingTableUnavailable() {
        DiningTable table = new DiningTable(3, 4);
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));

        DiningTable reserved = diningTableService.reserve(1L);

        assertThat(reserved.isAvailable()).isFalse();
    }

    @Test
    void reserve_throwsWhenTableDoesNotExist() {
        when(diningTableRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diningTableService.reserve(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void release_marksAReservedTableAvailableAgain() {
        DiningTable table = new DiningTable(3, 4);
        table.markReserved();
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));

        DiningTable released = diningTableService.release(1L);

        assertThat(released.isAvailable()).isTrue();
    }

    @Test
    void release_throwsWhenTableDoesNotExist() {
        when(diningTableRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diningTableService.release(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
