package com.example.smartkost.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Controller
public class PaymentFrontendController {

    private final RestTemplate restTemplate;

    @Value("${payment.service.url:http://localhost:8084}")
    private String paymentServiceUrl;

    public PaymentFrontendController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/payment/{orderId}/confirm")
    public String showQrisPaymentPage(@PathVariable String orderId, Model model) {
        try {
            // Provide orderId to the view so it can fetch data via AJAX
            model.addAttribute("orderId", orderId);
            return "payment-qris";
        } catch (Exception e) {
            model.addAttribute("error", "Terjadi kesalahan sistem");
            return "error";
        }
    }
}
