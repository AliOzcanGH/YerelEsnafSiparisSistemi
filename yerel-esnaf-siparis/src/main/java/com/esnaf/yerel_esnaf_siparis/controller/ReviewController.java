package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.Review;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.ProductService;
import com.esnaf.yerel_esnaf_siparis.service.ReviewService;
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
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review review, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User customer = userService.findByUsername(userDetails.getUsername());

            if (customer == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            review.setCustomer(customer);
            Review saved = reviewService.save(review);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public List<Review> getProductReviews(@PathVariable Long productId) {
        Optional<Product> product = productService.findById(productId);
        return product.map(reviewService::findByProduct).orElse(List.of());
    }

    @GetMapping("/product/{productId}/average")
    public Map<String, Object> getProductAverageRating(@PathVariable Long productId) {
        Optional<Product> product = productService.findById(productId);
        if (product.isPresent()) {
            double avg = reviewService.getAverageRating(product.get());
            List<Review> reviews = reviewService.findByProduct(product.get());
            return Map.of(
                "averageRating", avg,
                "totalReviews", reviews.size()
            );
        }
        return Map.of("averageRating", 0.0, "totalReviews", 0);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User customer = userService.findByUsername(userDetails.getUsername());

            Optional<Review> review = reviewService.findById(id);
            if (!review.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Yorum bulunamadı"));
            }

            if (!review.get().getCustomer().getId().equals(customer.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu yorumu silme yetkiniz yok"));
            }

            reviewService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Yorum silindi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

