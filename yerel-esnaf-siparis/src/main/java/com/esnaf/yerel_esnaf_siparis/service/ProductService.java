package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.Category;
import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryService categoryService;

    @Transactional(rollbackFor = Exception.class)
    public Product save(Product product) {
        try {
            // Null kontrolleri
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Ürün adı boş olamaz");
            }
            if (product.getPrice() < 0) {
                throw new IllegalArgumentException("Fiyat negatif olamaz");
            }
            if (product.getStock() < 0) {
                throw new IllegalArgumentException("Stok negatif olamaz");
            }
            
            Product saved = productRepository.save(product);
            productRepository.flush(); // SQLite için flush yap
            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ürün kaydedilemedi: " + e.getMessage(), e);
        }
    }

    public List<Product> findAll() {
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Hata durumunda boş liste döndür
        }
    }
    
    public List<Product> findByOwner(User owner) {
        return productRepository.findByOwner(owner);
    }
    
    public List<Product> findByCategoryId(Long categoryId) {
        Optional<Category> category = categoryService.findById(categoryId);
        return category.map(productRepository::findByCategory).orElse(List.of());
    }
    
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return productRepository.searchProducts(keyword);
    }
    
    public List<Product> searchAndFilter(String search, Long categoryId, Double minPrice, Double maxPrice) {
        try {
            List<Product> products = findAll();
            
            if (products == null || products.isEmpty()) {
                return products != null ? products : List.of();
            }
            
            // Arama
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                products = products.stream()
                    .filter(p -> p != null && p.getName() != null && 
                               (p.getName().toLowerCase().contains(searchLower) || 
                               (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower))))
                    .collect(Collectors.toList());
            }
            
            // Kategori filtresi
            if (categoryId != null) {
                products = products.stream()
                    .filter(p -> p != null && p.getCategory() != null && p.getCategory().getId() != null && 
                               p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
            }
            
            // Fiyat filtresi
            if (minPrice != null) {
                products = products.stream()
                    .filter(p -> p != null && p.getPrice() >= minPrice)
                    .collect(Collectors.toList());
            }
            
            if (maxPrice != null) {
                products = products.stream()
                    .filter(p -> p != null && p.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
            }
            
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Hata durumunda boş liste döndür
        }
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
