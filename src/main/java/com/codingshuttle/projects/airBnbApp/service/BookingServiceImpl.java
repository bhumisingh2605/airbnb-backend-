package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.dto.BookingDto;
import com.codingshuttle.projects.airBnbApp.dto.BookingRequest;
import com.codingshuttle.projects.airBnbApp.dto.GuestDto;
import com.codingshuttle.projects.airBnbApp.entity.Booking;
import com.codingshuttle.projects.airBnbApp.entity.Guest;
import com.codingshuttle.projects.airBnbApp.entity.User;
import com.codingshuttle.projects.airBnbApp.entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.entity.enums.PaymentStatus;
import com.codingshuttle.projects.airBnbApp.repository.BookingRepository;
import com.codingshuttle.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    // =========================
    // GET ALL BOOKINGS
    // =========================
    @Override
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // =========================
    // GET BOOKINGS FOR USER
    // =========================
    @Override
    public List<BookingDto> getBookingsForUser() {
        // TODO: Later filter by logged-in user
        return getAllBookings();
    }

    // =========================
    // INITIALISE BOOKING
    // =========================
    @Override
    public BookingDto initialiseBooking(BookingRequest request) {
        // 🔥 For now: pick the first user
        User user = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setRoomsCount(request.getRoomsCount() != null ? request.getRoomsCount() : 1);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.CREATED);
        booking.setAmount(request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO);

        Booking saved = bookingRepository.save(booking);
        return convertToDto(saved);
    }

    // =========================
    // ADD GUESTS
    // =========================
    @Override
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Set<Guest> guests = guestDtoList.stream()
                .map(dto -> {
                    Guest g = new Guest();
                    g.setName(dto.getName());
                    g.setAge(dto.getAge());
                    g.setGender(dto.getGender());
                    return g;
                })
                .collect(Collectors.toSet());

        booking.setGuests(guests);
        Booking saved = bookingRepository.save(booking);

        return convertToDto(saved);
    }

    // =========================
    // INITIATE PAYMENT (DUMMY)
    // =========================
    @Override
    public Map<String, Object> initiatePayment(Long bookingId) {
        return Map.of("status", "SUCCESS", "bookingId", bookingId);
    }

    // =========================
    // CANCEL BOOKING
    // =========================
    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // =========================
    // HELPER: Convert Booking entity → DTO
    // =========================
    private BookingDto convertToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setUser(booking.getUser());
        dto.setRoomsCount(booking.getRoomsCount());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setBookingStatus(booking.getBookingStatus());

        if (booking.getGuests() != null) {
            dto.setGuests(booking.getGuests().stream()
                    .map(g -> new GuestDto(g.getName(), g.getAge(), g.getGender()))
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
}