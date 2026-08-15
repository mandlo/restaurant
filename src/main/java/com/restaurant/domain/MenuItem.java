package com.restaurant.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MenuItemCategory category;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @Positive
    private int preparationMinutes;

    private boolean available = true;

    public MenuItem(String name, MenuItemCategory category, BigDecimal price, int preparationMinutes) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.preparationMinutes = preparationMinutes;
    }

    public void markUnavailable() {
        available = false;
    }

    public void markAvailable() {
        available = true;
    }

    public void updateDetails(String name, BigDecimal price, int preparationMinutes) {
        this.name = name;
        this.price = price;
        this.preparationMinutes = preparationMinutes;
    }
}
