package com.esnaf.yerel_esnaf_siparis.repository;

import com.esnaf.yerel_esnaf_siparis.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndActiveTrue(String code);
}

