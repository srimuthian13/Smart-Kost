package com.example.smartkost.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {



    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/tenant")
    public String tenantDashboard() {
        return "tenant/dashboard";
    }

    @GetMapping("/tenant/payment")
    public String tenantPayment() {
        return "tenant/payment";
    }
}