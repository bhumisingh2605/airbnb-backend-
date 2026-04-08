package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.dto.HotelDto;
import com.codingshuttle.projects.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/hotels") // ✅ FIXED PATH
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    // ✅ CREATE HOTEL
    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
        log.info("Creating hotel: {}", hotelDto.getName());
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    // ❌ REMOVED getAllHotels() to avoid conflict

    // ✅ GET HOTEL BY ID
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId) {
        return ResponseEntity.ok(
                hotelService.getHotelById(hotelId)
        );
    }

    // ✅ UPDATE HOTEL
    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(
            @PathVariable Long hotelId,
            @RequestBody HotelDto hotelDto
    ) {
        return ResponseEntity.ok(
                hotelService.updateHotelById(hotelId, hotelDto)
        );
    }

    // ✅ DELETE HOTEL
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId) {
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    // ✅ ACTIVATE HOTEL
    @PatchMapping("/{hotelId}/activate") // ✅ better REST design
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId) {
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }
}