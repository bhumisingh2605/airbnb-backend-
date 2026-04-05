package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.dto.BookingDto;
import com.codingshuttle.projects.airBnbApp.dto.BookingRequest;
import com.codingshuttle.projects.airBnbApp.dto.GuestDto;

import java.util.List;
import java.util.Map;

public interface BookingService {

    // Use single argument to match your DTO
    BookingDto initialiseBooking(BookingRequest request);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    Map<String, Object> initiatePayment(Long bookingId);

    void cancelBooking(Long bookingId);

    List<BookingDto> getBookingsForUser();

    List<BookingDto> getAllBookings();
}