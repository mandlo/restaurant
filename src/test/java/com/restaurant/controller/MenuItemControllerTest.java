package com.restaurant.controller;

import com.restaurant.domain.MenuItem;
import com.restaurant.domain.MenuItemCategory;
import com.restaurant.service.MenuItemService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuItemController.class)
class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuItemService menuItemService;

    @Test
    void create_returns201WithTheCreatedMenuItem() throws Exception {
        MenuItem pasta = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);
        when(menuItemService.create("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20))
                .thenReturn(pasta);

        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pasta","category":"MAIN","price":14.50,"preparationMinutes":20}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pasta"))
                .andExpect(jsonPath("$.category").value("MAIN"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void create_returns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","category":"MAIN","price":14.50,"preparationMinutes":20}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void findById_returns404WhenMenuItemDoesNotExist() throws Exception {
        when(menuItemService.findById(99L)).thenThrow(new EntityNotFoundException("Menu item 99 not found"));

        mockMvc.perform(get("/api/menu-items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Menu item 99 not found"));
    }

    @Test
    void findAvailableByCategory_returnsMatchingItems() throws Exception {
        MenuItem soup = new MenuItem("Soup", MenuItemCategory.STARTER, new BigDecimal("6.00"), 10);
        when(menuItemService.findAvailableByCategory(MenuItemCategory.STARTER)).thenReturn(List.of(soup));

        mockMvc.perform(get("/api/menu-items").param("category", "STARTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Soup"));
    }

    @Test
    void markUnavailable_returnsTheUpdatedMenuItem() throws Exception {
        MenuItem pasta = new MenuItem("Pasta", MenuItemCategory.MAIN, new BigDecimal("14.50"), 20);
        pasta.markUnavailable();
        when(menuItemService.markUnavailable(1L)).thenReturn(pasta);

        mockMvc.perform(patch("/api/menu-items/1/unavailable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}
