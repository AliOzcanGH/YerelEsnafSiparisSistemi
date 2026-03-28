package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.DashboardService;
import com.esnaf.yerel_esnaf_siparis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/shop-owner")
    public ResponseEntity<?> getShopOwnerStats(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User shopOwner = userService.findByUsername(userDetails.getUsername());

            if (shopOwner == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            if (shopOwner.getRole() != User.Role.SHOP_OWNER) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu sayfaya erişim yetkiniz yok"));
            }

            Map<String, Object> stats = dashboardService.getShopOwnerStats(shopOwner);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/shop-stats")
    public ResponseEntity<?> getShopStats(Authentication authentication) {
        // Yönlendirme için alternatif endpoint
        return getShopOwnerStats(authentication);
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerDashboard(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User customer = userService.findByUsername(userDetails.getUsername());

            if (customer == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            Map<String, Object> stats = dashboardService.getCustomerStats(customer);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/customer-stats")
    public ResponseEntity<?> getCustomerStats(Authentication authentication) {
        // Yönlendirme için alternatif endpoint
        return getCustomerDashboard(authentication);
    }
}

