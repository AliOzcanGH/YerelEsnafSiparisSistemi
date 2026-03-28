package com.esnaf.yerel_esnaf_siparis.repository;

import com.esnaf.yerel_esnaf_siparis.model.Category;
import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByOwner(User owner);
    List<Product> findByCategory(Category category);
    
    // Arama fonksiyonları
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER('%' || :keyword || '%') OR " +
           "LOWER(p.description) LIKE LOWER('%' || :keyword || '%')")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    // Fiyat aralığı
    List<Product> findByPriceBetween(double minPrice, double maxPrice);
    
    // Kategoriye göre ve fiyat aralığı
    List<Product> findByCategoryAndPriceBetween(Category category, double minPrice, double maxPrice);
}
