package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.ProductService;
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
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User owner = userService.findByUsername(userDetails.getUsername());

            if (owner == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            if (owner.getRole() != User.Role.SHOP_OWNER) {
                return ResponseEntity.status(403).body(Map.of("error", "Sadece esnaflar ürün ekleyebilir"));
            }

            // Category null olabilir, sorun değil
            if (product.getCategory() != null && product.getCategory().getId() == null) {
                product.setCategory(null); // Geçersiz category ID'yi temizle
            }

            product.setOwner(owner);
            product.setId(null); // Yeni ürün, ID null olmalı
            
            Product saved = productService.save(product);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ürün eklenirken hata oluştu: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User owner = userService.findByUsername(userDetails.getUsername());

            if (owner == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            Optional<Product> existingProduct = productService.findById(id);
            if (!existingProduct.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Ürün bulunamadı"));
            }

            Product existing = existingProduct.get();
            if (existing.getOwner() == null || !existing.getOwner().getId().equals(owner.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu ürünü güncelleme yetkiniz yok"));
            }

            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setStock(product.getStock());
            
            Product updated = productService.save(existing);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        try {
            List<Product> products = productService.searchAndFilter(search, categoryId, minPrice, maxPrice);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ürünler yüklenirken hata oluştu: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.search(keyword);
    }
    
    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.findByCategoryId(categoryId);
    }
    
    @GetMapping("/my-products")
    public ResponseEntity<?> getMyProducts(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User owner = userService.findByUsername(userDetails.getUsername());

            if (owner == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            List<Product> products = productService.findByOwner(owner);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public Optional<Product> getProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmanız gerekiyor"));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User owner = userService.findByUsername(userDetails.getUsername());

            if (owner == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            Optional<Product> existingProduct = productService.findById(id);
            if (!existingProduct.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Ürün bulunamadı"));
            }

            Product existing = existingProduct.get();
            if (existing.getOwner() == null || !existing.getOwner().getId().equals(owner.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu ürünü silme yetkiniz yok"));
            }

            productService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Ürün silindi"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
