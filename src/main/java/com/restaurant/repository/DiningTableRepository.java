package com.restaurant.repository;

import com.restaurant.domain.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    List<DiningTable> findBySeatsGreaterThanEqualAndAvailableTrue(int minimumSeats);
}
