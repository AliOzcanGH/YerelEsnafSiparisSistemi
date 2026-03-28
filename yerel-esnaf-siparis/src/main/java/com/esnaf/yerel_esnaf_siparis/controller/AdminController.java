package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CustomerOrderService orderService;
    
    @Autowired
    private CategoryService categoryService;

    // Admin yetkisi kontrolü
    private ResponseEntity<?> checkAdminAccess(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User admin = userService.findByUsername(userDetails.getUsername());

        if (admin == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
        }

        if (admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Bu sayfaya erişim yetkiniz yok"));
        }

        return null; // başarılı
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(Authentication authentication) {
        ResponseEntity<?> accessCheck = checkAdminAccess(authentication);
        if (accessCheck != null) return accessCheck;

        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.findAll().size());
            stats.put("totalProducts", productService.findAll().size());
            stats.put("totalOrders", orderService.findAll().size());
            stats.put("totalCategories", categoryService.findAll().size());
            
            // Kullanıcı dağılımı
            List<User> allUsers = userService.findAll();
            long customerCount = allUsers.stream().filter(u -> u.getRole() == User.Role.CUSTOMER).count();
            long shopOwnerCount = allUsers.stream().filter(u -> u.getRole() == User.Role.SHOP_OWNER).count();
            long adminCount = allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count();
            
            stats.put("customerCount", customerCount);
            stats.put("shopOwnerCount", shopOwnerCount);
            stats.put("adminCount", adminCount);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        ResponseEntity<?> accessCheck = checkAdminAccess(authentication);
        if (accessCheck != null) return accessCheck;

        try {
            return ResponseEntity.ok(userService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(Authentication authentication) {
        ResponseEntity<?> accessCheck = checkAdminAccess(authentication);
        if (accessCheck != null) return accessCheck;

        try {
            return ResponseEntity.ok(orderService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        ResponseEntity<?> accessCheck = checkAdminAccess(authentication);
        if (accessCheck != null) return accessCheck;

        try {
            // silinecek kullanıcının var olup olmadığını kontrol et
            var userOpt = userService.findById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }
            
            // kendi hesabını silmeye çalışıyorsa engelle
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentAdmin = userService.findByUsername(userDetails.getUsername());
            if (currentAdmin != null && currentAdmin.getId().equals(id)) {
                return ResponseEntity.status(400).body(Map.of("error", "Kendi hesabınızı silemezsiniz"));
            }
            
            userService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Kullanıcı ve ilişkili tüm veriler başarıyla silindi"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Kullanıcı silinirken hata oluştu: " + e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        ResponseEntity<?> accessCheck = checkAdminAccess(authentication);
        if (accessCheck != null) return accessCheck;

        try {
            productService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Ürün silindi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

