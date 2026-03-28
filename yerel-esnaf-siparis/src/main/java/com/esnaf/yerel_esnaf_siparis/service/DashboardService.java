package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.CustomerOrder;
import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private CustomerOrderService orderService;
    
    @Autowired
    private ProductService productService;

    public Map<String, Object> getShopOwnerStats(User shopOwner) {
        List<CustomerOrder> orders = orderService.findOrdersForShopOwner(shopOwner);
        List<Product> products = productService.findByOwner(shopOwner);
        
        // Toplam gelirc
        double totalRevenue = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProduct() != null && 
                   item.getProduct().getOwner() != null &&
                   item.getProduct().getOwner().getId().equals(shopOwner.getId()))
            .mapToDouble(item -> item.getPriceSnapshot() * item.getQuantity())
            .sum();
        
        // Toplam sipariş sayısı
        int totalOrders = orders.size();
        
        // Toplam ürün sayısı
        int totalProducts = products.size();
        
        // Düşük stok uyarısı (stok < 10)
        long lowStockCount = products.stream()
            .filter(p -> p.getStock() < 10)
            .count();
        
        // En çok satan ürünler
        Map<Product, Integer> productSales = new HashMap<>();
        orders.forEach(order -> {
            order.getItems().forEach(item -> {
                if (item.getProduct() != null && 
                    item.getProduct().getOwner() != null &&
                    item.getProduct().getOwner().getId().equals(shopOwner.getId())) {
                    productSales.merge(item.getProduct(), item.getQuantity(), Integer::sum);
                }
            });
        });
        
        List<Map<String, Object>> topProducts = productSales.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", entry.getKey().getId());
                map.put("productName", entry.getKey().getName());
                map.put("totalSold", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());
        
        // Son 7 günün satış verileri
        Calendar cal = Calendar.getInstance();
        Map<String, Double> dailySales = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            String dateKey = String.format("%td/%tm", cal, cal);
            dailySales.put(dateKey, 0.0);
        }
        
        orders.forEach(order -> {
            if (order.getOrderDate() != null) {
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTime(order.getOrderDate());
                String dateKey = String.format("%td/%tm", orderCal, orderCal);
                
                if (dailySales.containsKey(dateKey)) {
                    double orderTotal = order.getItems().stream()
                        .filter(item -> item.getProduct() != null && 
                               item.getProduct().getOwner() != null &&
                               item.getProduct().getOwner().getId().equals(shopOwner.getId()))
                        .mapToDouble(item -> item.getPriceSnapshot() * item.getQuantity())
                        .sum();
                    dailySales.put(dateKey, dailySales.get(dateKey) + orderTotal);
                }
            }
        });
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", totalOrders);
        stats.put("totalProducts", totalProducts);
        stats.put("lowStockCount", lowStockCount);
        stats.put("topProducts", topProducts);
        stats.put("dailySales", dailySales);
        
        return stats;
    }
    
    public Map<String, Object> getCustomerStats(User customer) {
        List<CustomerOrder> orders = orderService.findByCustomer(customer);
        
        // Toplam harcama
        double totalSpent = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .mapToDouble(item -> item.getPriceSnapshot() * item.getQuantity())
            .sum();
        
        // Toplam sipariş
        int totalOrders = orders.size();
        
        // En çok sipariş verdiği esnaflar
        Map<User, Integer> shopOwnerOrders = new HashMap<>();
        orders.forEach(order -> {
            order.getItems().forEach(item -> {
                if (item.getProduct() != null && item.getProduct().getOwner() != null) {
                    shopOwnerOrders.merge(item.getProduct().getOwner(), 1, Integer::sum);
                }
            });
        });
        
        List<Map<String, Object>> topShops = shopOwnerOrders.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("shopOwnerName", entry.getKey().getUsername());
                map.put("orderCount", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSpent", totalSpent);
        stats.put("totalOrders", totalOrders);
        stats.put("topShops", topShops);
        
        return stats;
    }
}

