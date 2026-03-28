package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.CustomerOrder;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.CustomerOrderService;
import com.esnaf.yerel_esnaf_siparis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {
    @Autowired
    private CustomerOrderService customerOrderService;
    
    @Autowired
    private UserService userService;

    @PostMapping
    public CustomerOrder createOrder(@RequestBody CustomerOrder customerOrder) {
        return customerOrderService.save(customerOrder);
    }

    @GetMapping
    public List<CustomerOrder> getAllOrders() {
        return customerOrderService.findAll();
    }
    
    @GetMapping("/shop-orders")
    public ResponseEntity<?> getShopOrders(Authentication authentication) {
        try {
            System.out.println("DEBUG: getShopOrders called");
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                System.out.println("DEBUG: No authentication");
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("DEBUG: Authenticated user: " + userDetails.getUsername());
            User shopOwner = userService.findByUsername(userDetails.getUsername());

            if (shopOwner == null) {
                System.out.println("DEBUG: User not found");
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            if (shopOwner.getRole() != User.Role.SHOP_OWNER) {
                System.out.println("DEBUG: User is not SHOP_OWNER, role: " + shopOwner.getRole());
                return ResponseEntity.status(403).body(Map.of("error", "Bu sayfaya erişim yetkiniz yok"));
            }

            System.out.println("DEBUG: About to call findOrdersForShopOwner for user: " + shopOwner.getUsername() + " (ID: " + shopOwner.getId() + ")");
            List<CustomerOrder> orders = customerOrderService.findOrdersForShopOwner(shopOwner);
            System.out.println("DEBUG: findOrdersForShopOwner returned " + orders.size() + " orders");
            
            // DEBUG: Tüm siparişleri de kontrol et
            List<CustomerOrder> allOrdersDebug = customerOrderService.findAll();
            System.out.println("DEBUG: Total orders in database: " + allOrdersDebug.size());
            for (CustomerOrder order : allOrdersDebug) {
                System.out.println("DEBUG: Order ID: " + order.getId() + 
                    ", Customer: " + (order.getCustomer() != null ? order.getCustomer().getUsername() : "null") +
                    ", Items count: " + (order.getItems() != null ? order.getItems().size() : 0));
            }
            
            for (CustomerOrder order : orders) {
                System.out.println("DEBUG: Filtered Order ID: " + order.getId() + ", Items: " + (order.getItems() != null ? order.getItems().size() : 0));
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getShopOrders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User shopOwner = userService.findByUsername(userDetails.getUsername());

            if (shopOwner == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            Optional<CustomerOrder> orderOpt = customerOrderService.findById(id);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Sipariş bulunamadı"));
            }

            CustomerOrder order = orderOpt.get();
            
            // Esnafın bu siparişte ürünü olup olmadığını kontrol et
            boolean hasProduct = order.getItems().stream()
                .anyMatch(item -> item.getProduct() != null 
                    && item.getProduct().getOwner() != null
                    && item.getProduct().getOwner().getId().equals(shopOwner.getId()));
            
            if (!hasProduct) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu siparişi güncelleme yetkiniz yok"));
            }

            String newStatus = request.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Durum boş olamaz"));
            }

            CustomerOrder updated = customerOrderService.updateStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public Optional<CustomerOrder> getOrderById(@PathVariable Long id) {
        return customerOrderService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        customerOrderService.deleteById(id);
    }
}
