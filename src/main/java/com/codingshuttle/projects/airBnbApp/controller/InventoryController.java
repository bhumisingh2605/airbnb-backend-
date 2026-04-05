package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.entity.Inventory;
import com.codingshuttle.projects.airBnbApp.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Get all available rooms for a specific date
     * Example: GET /api/inventory?date=2026-03-30
     */
    @GetMapping("/inventory")
    public List<Inventory> getAvailableRooms(@RequestParam String date) {
        // Convert the incoming string to a LocalDate
        LocalDate checkDate;
        try {
            checkDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        }

        // Call repository to fetch available inventory
        return inventoryRepository.findAvailableByDate(checkDate);
    }
}