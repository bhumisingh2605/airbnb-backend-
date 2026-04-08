package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.dto.BookingDto;
import com.codingshuttle.projects.airBnbApp.dto.BookingRequest;
import com.codingshuttle.projects.airBnbApp.dto.GuestDto;
import com.codingshuttle.projects.airBnbApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @GetMapping("/test")
    public String test() {
        return "backend Working";
    }

    // ✅ GET ALL BOOKINGS
    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // ✅ INITIALISE BOOKING (NO AUTH - FIXED)
    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(
            @RequestBody BookingRequest bookingRequest
    ) {
        BookingDto booking = bookingService.initialiseBooking(bookingRequest);
        return ResponseEntity.ok(booking);
    }

    // ✅ ADD GUESTS
    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(
            @PathVariable Long bookingId,
            @RequestBody List<GuestDto> guestDtoList
    ) {
        BookingDto booking = bookingService.addGuests(bookingId, guestDtoList);
        return ResponseEntity.ok(booking);
    }

    // ✅ INITIATE PAYMENT
    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @PathVariable Long bookingId
    ) {
        Map<String, Object> response =
                bookingService.initiatePayment(bookingId);

        return ResponseEntity.ok(response);
    }

    // ✅ CANCEL BOOKING
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId
    ) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}