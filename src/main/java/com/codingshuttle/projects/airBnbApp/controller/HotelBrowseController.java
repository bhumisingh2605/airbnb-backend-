package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.dto.HotelInfoDto;
import com.codingshuttle.projects.airBnbApp.dto.HotelPriceDto;
import com.codingshuttle.projects.airBnbApp.dto.HotelSearchRequest;
import com.codingshuttle.projects.airBnbApp.service.HotelService;
import com.codingshuttle.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/hotels")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    // ✅ DEFAULT API (VERY IMPORTANT - fixes your error)
    @GetMapping
    public ResponseEntity<Page<HotelPriceDto>> getAllHotels() {

        HotelSearchRequest request = new HotelSearchRequest();

        // 👉 default values (IMPORTANT)
        request.setCity(null); // show all cities
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());
        request.setRoomsCount(1);
        request.setPage(0);
        request.setSize(10);

        Page<HotelPriceDto> page = inventoryService.searchHotels(request);

        return ResponseEntity.ok(page);
    }

    // ✅ SEARCH HOTELS (used later from frontend search)
    @PostMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(
            @RequestBody HotelSearchRequest hotelSearchRequest
    ) {
        Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    // ✅ HOTEL DETAILS
    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}