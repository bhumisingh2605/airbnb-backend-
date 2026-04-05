package com.codingshuttle.projects.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomsCount;
    private BigDecimal totalAmount;  // ✅ This is required by BookingServiceImpl
}