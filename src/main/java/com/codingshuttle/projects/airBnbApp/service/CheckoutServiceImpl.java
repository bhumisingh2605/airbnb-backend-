package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.entity.Booking;
import com.codingshuttle.projects.airBnbApp.entity.User;
import com.codingshuttle.projects.airBnbApp.repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final BookingRepository bookingRepository;

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @Override
    public Map<String, Object> getCheckOutSession(Booking booking, String successUrl, String failureUrl) {

        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking cannot be null");
        }

        log.info("Creating Razorpay order for booking ID: {}", booking.getId());

        // 1. Authentication check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invalid principal type in security context");
        }

        // 2. Validate booking amount
        if (booking.getAmount() == null || booking.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid booking amount: must be greater than zero. Current value: " + booking.getAmount());
        }

        if (booking.getHotel() == null || booking.getRoom() == null) {
            log.warn("Booking {} is missing hotel or room reference", booking.getId());
        }

        try {
            // 3. Initialize Razorpay client
            RazorpayClient razorpay = new RazorpayClient(razorpayKey, razorpaySecret);

            // 4. Convert amount to paise
            int amountInPaise = booking.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .intValueExact();

            if (amountInPaise < 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Amount too small (minimum 1 INR)");
            }

            // 5. Create order request
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "book_" + booking.getId());

            // Metadata (notes)
            JSONObject notes = new JSONObject();
            notes.put("bookingId", String.valueOf(booking.getId()));
            notes.put("userId", String.valueOf(user.getId()));
            notes.put("userEmail", user.getEmail());

            if (booking.getHotel() != null) {
                notes.put("hotelName", booking.getHotel().getName());
            }

            orderRequest.put("notes", notes);

            // 6. Create Razorpay order
            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // 7. Save order ID
            booking.setRazorpayOrderId(razorpayOrderId);
            bookingRepository.save(booking);

            // 8. Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", razorpayOrderId);
            response.put("amount", amountInPaise);
            response.put("currency", "INR");

            // User details
            response.put("name", user.getName() != null ? user.getName() : "Guest User");
            response.put("email", user.getEmail() != null ? user.getEmail() : "no-email@guest.com");
            response.put("contact", user.getPhone() != null ? user.getPhone() : "");

            // Booking details
            if (booking.getHotel() != null) {
                response.put("hotelName", booking.getHotel().getName());
            }

            if (booking.getRoom() != null && booking.getRoom().getType() != null) {
                response.put("roomType", booking.getRoom().getType().toString());
            }

            response.put("checkIn",
                    booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "");

            response.put("checkOut",
                    booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "");

            log.info("Razorpay order created successfully → orderId: {}, amount: {} paise",
                    razorpayOrderId, amountInPaise);

            return response;

        } catch (RazorpayException e) {

            // FIXED: Removed getCode()
            log.error("Razorpay API error for booking {}: message={}",
                    booking.getId(), e.getMessage(), e);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment service error: " + e.getMessage()
            );

        } catch (Exception e) {

            log.error("Unexpected error while creating Razorpay order for booking {}",
                    booking.getId(), e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to initiate payment – please try again later"
            );
        }
    }
}