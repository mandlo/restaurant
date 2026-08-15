package com.restaurant.repository;

import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategoryAndAvailableTrue(MenuItemCategory category);
}
