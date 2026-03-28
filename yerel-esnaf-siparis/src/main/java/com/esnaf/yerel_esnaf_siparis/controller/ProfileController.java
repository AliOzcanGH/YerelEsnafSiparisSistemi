package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            // Güncelleme
            if (updates.containsKey("email")) {
                user.setEmail(updates.get("email"));
            }
            if (updates.containsKey("phone")) {
                user.setPhone(updates.get("phone"));
            }
            if (updates.containsKey("address")) {
                user.setAddress(updates.get("address"));
            }
            if (updates.containsKey("fullName")) {
                user.setFullName(updates.get("fullName"));
            }

            User updated = userService.save(user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.status(400).body(Map.of("error", "Mevcut şifre yanlış"));
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            return ResponseEntity.ok(Map.of("message", "Şifre başarıyla değiştirildi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

