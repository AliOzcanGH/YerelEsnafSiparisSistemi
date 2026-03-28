package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.Review;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public Review save(Review review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(new Date());
        }
        return reviewRepository.save(review);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public List<Review> findByCustomer(User customer) {
        return reviewRepository.findByCustomer(customer);
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }
    
    public double getAverageRating(Product product) {
        List<Review> reviews = findByProduct(product);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
    }
}

