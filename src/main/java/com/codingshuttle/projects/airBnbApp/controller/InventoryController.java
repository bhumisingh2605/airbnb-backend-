package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.entity.Inventory;
import com.codingshuttle.projects.airBnbApp.entity.Hotel;
import com.codingshuttle.projects.airBnbApp.repository.InventoryRepository;
import com.codingshuttle.projects.airBnbApp.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final HotelRepository hotelRepository; // ✅ IMPORTANT

    // ✅ CREATE INVENTORY (PUT YOUR CODE HERE)
    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {

        // ✅ Fetch hotel from DB
        Hotel hotel = hotelRepository.findById(inventory.getHotel().getId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // ✅ Set hotel object
        inventory.setHotel(hotel);

        // ✅ Defaults
        if (inventory.getBookedCount() == null) {
            inventory.setBookedCount(0);
        }

        if (inventory.getClosed() == null) {
            inventory.setClosed(false);
        }

        // ✅ Auto-set city
        inventory.setCity(hotel.getCity());

        return inventoryRepository.save(inventory);
    }

    // ✅ GET INVENTORY BY DATE
    @GetMapping
    public List<Inventory> getAvailableRooms(@RequestParam String date) {

        LocalDate checkDate = LocalDate.parse(date);

        return inventoryRepository.findAvailableByDate(checkDate);
    }

    // ✅ GET ALL (optional)
    @GetMapping("/all")
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
}