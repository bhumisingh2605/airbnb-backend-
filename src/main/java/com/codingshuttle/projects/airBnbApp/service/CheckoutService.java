package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.entity.Booking;

import java.util.Map;

public interface CheckoutService {
    Map<String, Object> getCheckOutSession(Booking booking, String successUrl, String failureUrl);
}