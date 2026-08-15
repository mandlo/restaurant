package com.restaurant.service;

import com.restaurant.domain.DiningTable;
import com.restaurant.repository.DiningTableRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiningTableService {

    private final DiningTableRepository diningTableRepository;

    public DiningTableService(DiningTableRepository diningTableRepository) {
        this.diningTableRepository = diningTableRepository;
    }

    @Transactional(readOnly = true)
    public List<DiningTable> findAvailableWithMinimumSeats(int minimumSeats) {
        return diningTableRepository.findBySeatsGreaterThanEqualAndAvailableTrue(minimumSeats);
    }

    @Transactional
    public DiningTable reserve(Long id) {
        DiningTable table = findOrThrow(id);
        table.markReserved();
        return table;
    }

    @Transactional
    public DiningTable release(Long id) {
        DiningTable table = findOrThrow(id);
        table.markAvailable();
        return table;
    }

    private DiningTable findOrThrow(Long id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dining table " + id + " not found"));
    }
}
