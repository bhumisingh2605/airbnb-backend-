package com.codingshuttle.projects.airBnbApp.controller;

import com.codingshuttle.projects.airBnbApp.service.PaymentService;
import com.razorpay.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<String> handleRazorpayWebhook(HttpServletRequest request,
                                                        @RequestHeader("X-Razorpay-Signature") String razorpaySignature) {
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Unable to read request");
        }

        try {
            // Verify webhook signature
            Utils.verifyWebhookSignature(payload.toString(), razorpaySignature, webhookSecret);

            // Parse event payload
            JSONObject eventJson = new JSONObject(payload.toString());
            String eventType = eventJson.getString("event");

            // Handle the event
            paymentService.capturePayment(eventJson);

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook verification failed: " + e.getMessage());
        }
    }
}