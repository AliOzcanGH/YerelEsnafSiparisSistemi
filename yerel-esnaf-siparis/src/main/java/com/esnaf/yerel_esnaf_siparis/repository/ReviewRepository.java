package com.esnaf.yerel_esnaf_siparis.repository;

import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.Review;
import com.esnaf.yerel_esnaf_siparis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByCustomer(User customer);
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
}

