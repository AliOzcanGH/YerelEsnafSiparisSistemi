package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.*;
import com.esnaf.yerel_esnaf_siparis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {
    
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerOrderService customerOrderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private CouponService couponService;
    
    @GetMapping("/")
    public String index(Model model) {
        try {
            List<Product> products = productService.findAll();
            model.addAttribute("products", products != null ? products : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("products", new ArrayList<>());
            model.addAttribute("error", "Ürünler yüklenirken bir hata oluştu: " + e.getMessage());
        }
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("user") User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }
    
    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }
    
    @GetMapping("/orders")
    public String orders(Model model, Authentication authentication) {
        System.out.println("DEBUG: /orders endpoint called");
        List<CustomerOrder> orders = new ArrayList<>();
        Map<Long, Double> orderTotals = new java.util.HashMap<>();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            try {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                System.out.println("DEBUG: /orders - Authenticated user: " + userDetails.getUsername());
                User customer = userService.findByUsername(userDetails.getUsername());
                
                if (customer != null) {
                    System.out.println("DEBUG: /orders - Customer found, ID: " + customer.getId());
                    orders = customerOrderService.findByCustomer(customer);
                    System.out.println("DEBUG: /orders - Found " + orders.size() + " orders");
                    
                    // Her sipariş için toplam tutarı hesapla ve Map'e ekle
                    for (CustomerOrder order : orders) {
                        double total = 0;
                        if (order.getItems() != null && !order.getItems().isEmpty()) {
                            System.out.println("DEBUG: /orders - Order " + order.getId() + " has " + order.getItems().size() + " items");
                            for (OrderItem item : order.getItems()) {
                                if (item != null) {
                                    total += item.getPriceSnapshot() * item.getQuantity();
                                }
                            }
                        } else {
                            System.out.println("DEBUG: /orders - Order " + order.getId() + " has no items");
                        }
                        orderTotals.put(order.getId(), total);
                    }
                } else {
                    System.out.println("DEBUG: /orders - Customer is null");
                }
            } catch (Exception e) {
                // Hata durumunda boş liste döndür
                System.out.println("DEBUG: /orders - Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("DEBUG: /orders - No authentication");
        }
        
        System.out.println("DEBUG: /orders - Returning " + orders.size() + " orders to view");
        model.addAttribute("orders", orders);
        model.addAttribute("orderTotals", orderTotals);
        return "orders";
    }
    
    @GetMapping("/test")
    public String test() {
        return "test";
    }
    
    @GetMapping("/my-products")
    public String myProducts() {
        return "my-products";
    }
    
    @GetMapping("/shop-orders")
    public String shopOrders() {
        return "shop-orders";
    }
    
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
    
    @GetMapping("/create-admin")
    @ResponseBody
    public Map<String, String> createAdmin() {
        try {
            // Zaten admin varsa oluşturma
            User existingAdmin = userService.findByUsername("admin");
            if (existingAdmin != null) {
                return Map.of("error", "Admin kullanıcısı zaten mevcut", "username", "admin", "password", "password");
            }
            
            // Yeni admin oluştur
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("password"); // registerUser şifreleyecek
            admin.setEmail("admin@test.com");
            admin.setFullName("Sistem Yöneticisi");
            admin.setRole(User.Role.ADMIN);
            
            userService.registerUser(admin);
            
            return Map.of(
                "message", "Admin kullanıcısı oluşturuldu!",
                "username", "admin",
                "password", "password",
                "loginUrl", "/login"
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/api/checkout")
    @ResponseBody
    public Map<String, Object> checkout(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            // Giriş yapmış kullanıcıyı al
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return Map.of("success", false, "message", "Giriş yapmanız gerekiyor");
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User customer = userService.findByUsername(userDetails.getUsername());
            
            if (customer == null) {
                return Map.of("success", false, "message", "Kullanıcı bulunamadı");
            }
            
            // Request'ten cart items ve coupon code'u al
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) request.get("items");
            String couponCode = request.containsKey("couponCode") ? (String) request.get("couponCode") : null;
            
            if (cartItems == null || cartItems.isEmpty()) {
                return Map.of("success", false, "message", "Sepetiniz boş");
            }
            
            // Sepet toplamını hesapla
            double subtotal = 0;
            for (Map<String, Object> item : cartItems) {
                double price = Double.parseDouble(item.get("price").toString());
                int quantity = Integer.parseInt(item.get("quantity").toString());
                subtotal += price * quantity;
            }
            
            // Kupon doğrulama ve indirim hesaplama
            double discount = 0;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                try {
                    var couponOpt = couponService.findByCode(couponCode.trim());
                    if (couponOpt.isPresent()) {
                        var coupon = couponOpt.get();
                        if (couponService.isValid(coupon, subtotal)) {
                            discount = couponService.calculateDiscount(coupon, subtotal);
                        }
                    }
                } catch (Exception e) {
                    // Kupon hatası, devam et (indirim yok)
                    System.err.println("Kupon doğrulama hatası: " + e.getMessage());
                }
            }
            
            // Sipariş oluştur - Service metodunda transaction yönetimi ile
            CustomerOrder order = customerOrderService.createCompleteOrder(customer, cartItems, productService, orderItemService);
            System.out.println("DEBUG: Checkout - Order created successfully! ID: " + order.getId());
            
            String message = "Siparişiniz başarıyla oluşturuldu!";
            if (discount > 0) {
                message += " İndirim: ₺" + String.format("%.2f", discount);
            }
            
            return Map.of(
                "success", true, 
                "message", message,
                "orderId", order.getId()
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "Hata: " + e.getMessage());
        }
    }
    
}
