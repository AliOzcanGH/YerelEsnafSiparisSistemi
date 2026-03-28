package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.Coupon;
import com.esnaf.yerel_esnaf_siparis.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @PostMapping
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        return couponService.save(coupon);
    }

    @GetMapping
    public List<Coupon> getAllCoupons() {
        return couponService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Coupon> getCouponById(@PathVariable Long id) {
        return couponService.findById(id);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> request) {
        try {
            // code kontrolü
            if (!request.containsKey("code") || request.get("code") == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Kupon kodu boş olamaz"));
            }
            
            String code = request.get("code").toString().trim();
            if (code.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Kupon kodu boş olamaz"));
            }
            
            // order amount kontrolü
            if (!request.containsKey("orderAmount") || request.get("orderAmount") == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Sipariş tutarı belirtilmelidir"));
            }
            
            double orderAmount;
            try {
                orderAmount = Double.parseDouble(request.get("orderAmount").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(400).body(Map.of("error", "Geçersiz sipariş tutarı"));
            }
            
            if (orderAmount <= 0) {
                return ResponseEntity.status(400).body(Map.of("error", "Sipariş tutarı sıfırdan büyük olmalıdır"));
            }

            Optional<Coupon> couponOpt = couponService.findByCode(code);
            if (!couponOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Kupon bulunamadı"));
            }

            Coupon coupon = couponOpt.get();
            if (!couponService.isValid(coupon, orderAmount)) {
                return ResponseEntity.status(400).body(Map.of("error", "Kupon geçersiz veya minimum sipariş tutarı karşılanmıyor"));
            }

            double discount = couponService.calculateDiscount(coupon, orderAmount);
            double finalAmount = orderAmount - discount;
            
            // dscription null olabilir
            String description = coupon.getDescription();
            if (description == null) {
                description = "";
            }

            return ResponseEntity.ok(Map.of(
                "valid", true,
                "discount", discount,
                "discountPercentage", coupon.getDiscountPercentage(),
                "finalAmount", finalAmount,
                "couponDescription", description
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Log için
            return ResponseEntity.status(500).body(Map.of("error", "Sunucu hatası: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public Coupon updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        return couponService.save(coupon);
    }

    @DeleteMapping("/{id}")
    public void deleteCoupon(@PathVariable Long id) {
        couponService.deleteById(id);
    }
}

