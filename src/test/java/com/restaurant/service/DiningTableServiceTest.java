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
import static org.mockito.ArgumentMatchers.any;
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
    void create_savesAndReturnsANewTable() {
        when(diningTableRepository.save(any(DiningTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiningTable created = diningTableService.create(5, 4);

        assertThat(created.getTableNumber()).isEqualTo(5);
        assertThat(created.getSeats()).isEqualTo(4);
        assertThat(created.isAvailable()).isTrue();
    }

    @Test
    void findById_returnsAnExistingTable() {
        DiningTable table = new DiningTable(3, 4);
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));

        assertThat(diningTableService.findById(1L)).isSameAs(table);
    }

    @Test
    void findById_throwsWhenTableDoesNotExist() {
        when(diningTableRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diningTableService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class);
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

    @Test
    void updateSeats_changesSeatCountOnAnExistingTable() {
        DiningTable table = new DiningTable(3, 4);
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));

        DiningTable updated = diningTableService.updateSeats(1L, 6);

        assertThat(updated.getSeats()).isEqualTo(6);
    }

    @Test
    void updateSeats_throwsWhenTableDoesNotExist() {
        when(diningTableRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diningTableService.updateSeats(1L, 6))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
