package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.OrderItem;
import com.esnaf.yerel_esnaf_siparis.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;

    public OrderItem save(OrderItem orderItem) {
        try {
            OrderItem saved = orderItemRepository.save(orderItem);
            orderItemRepository.flush(); // SQLite için flush
            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Sipariş kalemi kaydedilemedi: " + e.getMessage(), e);
        }
    }

    public List<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }

    public Optional<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }

    public void deleteById(Long id) {
        orderItemRepository.deleteById(id);
    }
}
