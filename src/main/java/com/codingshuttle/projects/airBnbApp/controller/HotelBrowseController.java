package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.dto.HotelPriceDto;
import com.codingshuttle.projects.airBnbApp.dto.HotelSearchRequest;
import com.codingshuttle.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/hotels")
@CrossOrigin(origins = "*") // allow frontend access after deployment
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;

    // ✅ GET ALL HOTELS (default listing)
    @GetMapping
    public ResponseEntity<Page<HotelPriceDto>> getAllHotels() {

        HotelSearchRequest request = new HotelSearchRequest();

        // Default values
        request.setCity(null);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());
        request.setRoomsCount(1);
        request.setPage(0);
        request.setSize(10);

        Page<HotelPriceDto> hotels = inventoryService.searchHotels(request);

        return ResponseEntity.ok(hotels);
    }

    // ✅ SEARCH HOTELS (filters from frontend)
    @PostMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(
            @RequestBody HotelSearchRequest hotelSearchRequest
    ) {

        Page<HotelPriceDto> hotels =
                inventoryService.searchHotels(hotelSearchRequest);

        return ResponseEntity.ok(hotels);
    }
}