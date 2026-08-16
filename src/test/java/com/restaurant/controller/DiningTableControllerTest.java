package com.restaurant.controller;

import com.restaurant.domain.DiningTable;
import com.restaurant.service.DiningTableService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiningTableController.class)
class DiningTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiningTableService diningTableService;

    @Test
    void create_returns201WithTheCreatedTable() throws Exception {
        DiningTable table = new DiningTable(5, 4);
        when(diningTableService.create(5, 4)).thenReturn(table);

        mockMvc.perform(post("/api/dining-tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":5,"seats":4}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value(5))
                .andExpect(jsonPath("$.seats").value(4))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void create_returns400WhenSeatsIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/dining-tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":5,"seats":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void findById_returns404WhenTableDoesNotExist() throws Exception {
        when(diningTableService.findById(99L)).thenThrow(new EntityNotFoundException("Dining table 99 not found"));

        mockMvc.perform(get("/api/dining-tables/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dining table 99 not found"));
    }

    @Test
    void findAvailableWithMinimumSeats_returnsMatchingTables() throws Exception {
        DiningTable table = new DiningTable(2, 6);
        when(diningTableService.findAvailableWithMinimumSeats(4)).thenReturn(List.of(table));

        mockMvc.perform(get("/api/dining-tables").param("minSeats", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tableNumber").value(2));
    }

    @Test
    void updateSeats_returnsTheUpdatedTable() throws Exception {
        DiningTable table = new DiningTable(3, 6);
        when(diningTableService.updateSeats(1L, 6)).thenReturn(table);

        mockMvc.perform(put("/api/dining-tables/1/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"seats":6}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats").value(6));
    }

    @Test
    void reserve_returnsTheReservedTable() throws Exception {
        DiningTable table = new DiningTable(3, 4);
        table.markReserved();
        when(diningTableService.reserve(1L)).thenReturn(table);

        mockMvc.perform(patch("/api/dining-tables/1/reserve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}
