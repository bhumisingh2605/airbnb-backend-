package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.entity.Booking;
import com.codingshuttle.projects.airBnbApp.entity.enums.BookingStatus;
import com.codingshuttle.projects.airBnbApp.entity.enums.PaymentStatus;
import com.codingshuttle.projects.airBnbApp.repository.BookingRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BookingRepository bookingRepository;

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    // =========================
    // 🔥 WEBHOOK HANDLER
    // =========================
    @Transactional
    public void capturePayment(JSONObject eventJson) {

        String eventType = eventJson.getString("event");
        log.info("Received Razorpay event: {}", eventType);

        switch (eventType) {

            // =========================
            // ✅ PAYMENT SUCCESS
            // =========================
            case "payment.captured" -> {

                JSONObject paymentEntity = eventJson
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String paymentId = paymentEntity.getString("id");
                String orderId = paymentEntity.getString("order_id");
                int amount = paymentEntity.getInt("amount");
                String currency = paymentEntity.getString("currency");

                Booking booking = bookingRepository
                        .findByRazorpayOrderId(orderId)
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

                // 🔥 Idempotency check
                if (booking.getRazorpayPaymentId() != null) {
                    log.warn("Duplicate payment webhook ignored for orderId: {}", orderId);
                    return;
                }

                // ✅ Update booking
                booking.setRazorpayPaymentId(paymentId);
                booking.setPaymentStatus(PaymentStatus.PAID);
                booking.setPaymentTime(LocalDateTime.now());

                booking.setAmountPaid(
                        BigDecimal.valueOf(amount)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );

                booking.setCurrency(currency);
                booking.setBookingStatus(BookingStatus.CONFIRMED);

                bookingRepository.save(booking);

                log.info("Booking CONFIRMED for orderId: {}", orderId);
            }

            // =========================
            // ❌ PAYMENT FAILED
            // =========================
            case "payment.failed" -> {

                JSONObject paymentEntity = eventJson
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String orderId = paymentEntity.getString("order_id");

                Booking booking = bookingRepository
                        .findByRazorpayOrderId(orderId)
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

                booking.setPaymentStatus(PaymentStatus.FAILED);
                booking.setBookingStatus(BookingStatus.CANCELLED);

                bookingRepository.save(booking);

                log.warn("Payment FAILED for orderId: {}", orderId);
            }

            // =========================
            // 🔁 REFUND PROCESSED
            // =========================
            case "refund.processed" -> {

                JSONObject refundEntity = eventJson
                        .getJSONObject("payload")
                        .getJSONObject("refund")
                        .getJSONObject("entity");

                String paymentId = refundEntity.getString("payment_id");
                String refundId = refundEntity.getString("id");
                int refundAmount = refundEntity.getInt("amount");

                Booking booking = bookingRepository
                        .findByRazorpayPaymentId(paymentId)
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

                // 🔥 Idempotency check
                if (booking.getPaymentStatus() == PaymentStatus.REFUNDED) {
                    log.warn("Duplicate refund webhook ignored for paymentId: {}", paymentId);
                    return;
                }

                booking.setPaymentStatus(PaymentStatus.REFUNDED);
                booking.setBookingStatus(BookingStatus.CANCELLED);
                booking.setRazorpayRefundId(refundId);

                BigDecimal currentAmount = booking.getAmountPaid() != null
                        ? booking.getAmountPaid()
                        : BigDecimal.ZERO;

                BigDecimal refundAmt = BigDecimal.valueOf(refundAmount)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                booking.setAmountPaid(currentAmount.subtract(refundAmt));

                bookingRepository.save(booking);

                log.info("Booking REFUNDED for paymentId: {}", paymentId);
            }

            // =========================
            // ⚠️ UNKNOWN EVENT
            // =========================
            default -> log.warn("Unhandled Razorpay event: {}", eventType);
        }
    }

    // =========================
    // 🔥 CREATE REFUND API
    // =========================
    public void createRefund(String paymentId) {

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("payment_id", paymentId);

            Refund refund = razorpay.payments.refund(paymentId, refundRequest);

            log.info("Refund created: paymentId={}, refundId={}",
                    paymentId, refund.get("id"));

        } catch (Exception e) {

            log.error("Refund failed for paymentId: {}", paymentId, e);
            throw new RuntimeException("Refund failed", e);
        }
    }
}