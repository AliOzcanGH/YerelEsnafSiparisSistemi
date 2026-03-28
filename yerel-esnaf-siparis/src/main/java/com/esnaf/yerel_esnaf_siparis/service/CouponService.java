package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.Coupon;
import com.esnaf.yerel_esnaf_siparis.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    public Optional<Coupon> findById(Long id) {
        return couponRepository.findById(id);
    }

    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return couponRepository.findByCodeAndActiveTrue(code.trim());
    }

    public boolean isValid(Coupon coupon, double orderAmount) {
        if (coupon == null || !coupon.isActive()) {
            return false;
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().before(new Date())) {
            return false;
        }
        return orderAmount >= coupon.getMinOrderAmount();
    }

    public double calculateDiscount(Coupon coupon, double orderAmount) {
        if (!isValid(coupon, orderAmount)) {
            return 0;
        }
        if (orderAmount <= 0 || coupon.getDiscountPercentage() <= 0) {
            return 0;
        }
        return orderAmount * (coupon.getDiscountPercentage() / 100);
    }

    public void deleteById(Long id) {
        couponRepository.deleteById(id);
    }
}

