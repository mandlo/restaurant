package com.restaurant.controller;

import com.restaurant.controller.dto.DiningTableRequest;
import com.restaurant.controller.dto.DiningTableResponse;
import com.restaurant.controller.dto.UpdateSeatsRequest;
import com.restaurant.domain.DiningTable;
import com.restaurant.service.DiningTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dining-tables")
@Tag(name = "Dining tables")
public class DiningTableController {

    private final DiningTableService diningTableService;

    public DiningTableController(DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a dining table")
    public DiningTableResponse create(@Valid @RequestBody DiningTableRequest request) {
        DiningTable created = diningTableService.create(request.tableNumber(), request.seats());
        return DiningTableResponse.from(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a dining table by id")
    public DiningTableResponse findById(@PathVariable Long id) {
        return DiningTableResponse.from(diningTableService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List available tables with at least the given number of seats")
    public List<DiningTableResponse> findAvailableWithMinimumSeats(@RequestParam int minSeats) {
        return diningTableService.findAvailableWithMinimumSeats(minSeats).stream()
                .map(DiningTableResponse::from)
                .toList();
    }

    @PutMapping("/{id}/seats")
    @Operation(summary = "Change a table's seat count")
    public DiningTableResponse updateSeats(@PathVariable Long id, @Valid @RequestBody UpdateSeatsRequest request) {
        return DiningTableResponse.from(diningTableService.updateSeats(id, request.seats()));
    }

    @PatchMapping("/{id}/reserve")
    @Operation(summary = "Reserve a table")
    public DiningTableResponse reserve(@PathVariable Long id) {
        return DiningTableResponse.from(diningTableService.reserve(id));
    }

    @PatchMapping("/{id}/release")
    @Operation(summary = "Release a reserved table")
    public DiningTableResponse release(@PathVariable Long id) {
        return DiningTableResponse.from(diningTableService.release(id));
    }
}
