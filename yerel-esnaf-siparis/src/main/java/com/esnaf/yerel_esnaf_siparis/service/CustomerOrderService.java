package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.CustomerOrder;
import com.esnaf.yerel_esnaf_siparis.model.OrderItem;
import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.repository.CustomerOrderRepository;
import com.esnaf.yerel_esnaf_siparis.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerOrderService {
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    
    @Autowired
    private ProductRepository productRepository;

    public CustomerOrder save(CustomerOrder customerOrder) {
        try {
            if (customerOrder.getOrderDate() == null) {
                customerOrder.setOrderDate(new Date());
            }
            CustomerOrder saved = customerOrderRepository.save(customerOrder);
            customerOrderRepository.flush(); // SQLite için flush
            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Sipariş kaydedilemedi: " + e.getMessage(), e);
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CustomerOrder> findAll() {
        return customerOrderRepository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CustomerOrder> findByCustomer(User customer) {
        try {
            System.out.println("DEBUG: findByCustomer - customer ID: " + customer.getId() + ", username: " + customer.getUsername());
            List<CustomerOrder> orders = customerOrderRepository.findByCustomer(customer);
            System.out.println("DEBUG: findByCustomer - found " + orders.size() + " orders");
            for (CustomerOrder order : orders) {
                System.out.println("DEBUG: Order ID: " + order.getId() + ", Items count: " + (order.getItems() != null ? order.getItems().size() : 0));
            }
            // Tarihe göre descending sırala (en yeni önce)
            orders.sort((o1, o2) -> {
                if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                if (o1.getOrderDate() == null) return 1;
                if (o2.getOrderDate() == null) return -1;
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            });
            return orders;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CustomerOrder> findOrdersForShopOwner(User shopOwner) {
        try {
            System.out.println("========================================");
            System.out.println("DEBUG: findOrdersForShopOwner START");
            System.out.println("DEBUG: shopOwner ID: " + shopOwner.getId() + ", username: " + shopOwner.getUsername());
            
            // Tüm siparişleri al
            List<CustomerOrder> allOrders = customerOrderRepository.findAll();
            System.out.println("DEBUG: Total orders in DB: " + allOrders.size());
            
            if (allOrders.isEmpty()) {
                System.out.println("DEBUG: No orders found in database at all!");
                System.out.println("DEBUG: findOrdersForShopOwner END - returning empty list");
                System.out.println("========================================");
                return new ArrayList<>();
            }
            
            // Esnafın ürünlerini içeren siparişleri filtrele
            List<CustomerOrder> shopOwnerOrders = new ArrayList<>();
            
            for (CustomerOrder order : allOrders) {
                System.out.println("DEBUG: Checking order ID: " + order.getId());
                
                if (order.getItems() == null || order.getItems().isEmpty()) {
                    System.out.println("DEBUG: Order " + order.getId() + " has no items");
                    continue;
                }
                
                System.out.println("DEBUG: Order " + order.getId() + " has " + order.getItems().size() + " items");
                
                // Siparişin en az bir ürünü bu esnafa aitse ekle
                boolean hasMyProduct = false;
                for (OrderItem item : order.getItems()) {
                    if (item == null) {
                        System.out.println("DEBUG: OrderItem is null");
                        continue;
                    }
                    
                    System.out.println("DEBUG: Checking OrderItem ID: " + item.getId());
                    
                    if (item.getProduct() == null) {
                        System.out.println("DEBUG: OrderItem " + item.getId() + " has no product");
                        continue;
                    }
                    
                    Long productId = item.getProduct().getId();
                    System.out.println("DEBUG: Product ID: " + productId);
                    
                    // Owner ID'yi veritabanından direkt sorgula (daha güvenli)
                    Long productOwnerId = null;
                    try {
                        Optional<Product> productOpt = productRepository.findById(productId);
                        if (productOpt.isPresent()) {
                            Product product = productOpt.get();
                            System.out.println("DEBUG: Product loaded from DB: " + product.getName());
                            
                            // Owner'ı kontrol et
                            try {
                                User productOwner = product.getOwner();
                                if (productOwner != null) {
                                    productOwnerId = productOwner.getId();
                                    System.out.println("DEBUG: Product " + productId + " owner ID from getOwner(): " + productOwnerId);
                                } else {
                                    // Owner null ise, getOwnerId() helper metodunu dene
                                    productOwnerId = product.getOwnerId();
                                    System.out.println("DEBUG: Product " + productId + " owner ID from getOwnerId(): " + productOwnerId);
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: Error accessing owner: " + e.getMessage());
                                e.printStackTrace();
                                // Son çare: getOwnerId() helper metodunu dene
                                try {
                                    productOwnerId = product.getOwnerId();
                                    System.out.println("DEBUG: Product " + productId + " owner ID from getOwnerId() (fallback): " + productOwnerId);
                                } catch (Exception e2) {
                                    System.out.println("DEBUG: getOwnerId() also failed: " + e2.getMessage());
                                }
                            }
                        } else {
                            System.out.println("DEBUG: Product " + productId + " not found in database");
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Exception loading product from DB: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    if (productOwnerId == null) {
                        System.out.println("DEBUG: Product " + productId + " has no owner ID, skipping");
                        continue;
                    }
                    
                    System.out.println("DEBUG: Product " + productId + " owner ID: " + productOwnerId + ", shopOwner ID: " + shopOwner.getId());
                    System.out.println("DEBUG: Comparing: productOwnerId=" + productOwnerId + " (type: " + (productOwnerId != null ? productOwnerId.getClass().getName() : "null") + ")");
                    System.out.println("DEBUG: Comparing: shopOwnerId=" + shopOwner.getId() + " (type: " + shopOwner.getId().getClass().getName() + ")");
                    System.out.println("DEBUG: Are they equal? " + (productOwnerId != null && productOwnerId.equals(shopOwner.getId())));
                    
                    if (productOwnerId != null && productOwnerId.equals(shopOwner.getId())) {
                        hasMyProduct = true;
                        System.out.println("DEBUG: ✓ MATCH! Order " + order.getId() + " contains product " + productId + " owned by shopOwner");
                        break;
                    } else {
                        System.out.println("DEBUG: ✗ NO MATCH - Product owner ID (" + productOwnerId + ") != shopOwner ID (" + shopOwner.getId() + ")");
                    }
                }
                
                if (hasMyProduct) {
                    shopOwnerOrders.add(order);
                    System.out.println("DEBUG: Added order " + order.getId() + " to shopOwnerOrders");
                }
            }
            
            System.out.println("DEBUG: Filtered orders count: " + shopOwnerOrders.size());
            
            if (shopOwnerOrders.isEmpty()) {
                System.out.println("DEBUG: WARNING - No orders matched the shopOwner filter!");
                System.out.println("DEBUG: This could mean:");
                System.out.println("DEBUG:   1. Products in orders don't have owner set");
                System.out.println("DEBUG:   2. Product owner IDs don't match shopOwner ID: " + shopOwner.getId());
                System.out.println("DEBUG:   3. Orders have no items");
            }
            
            // Tarihe göre descending sırala
            shopOwnerOrders.sort((o1, o2) -> {
                if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                if (o1.getOrderDate() == null) return 1;
                if (o2.getOrderDate() == null) return -1;
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            });
            
            System.out.println("DEBUG: findOrdersForShopOwner END - returning " + shopOwnerOrders.size() + " orders");
            System.out.println("========================================");
            return shopOwnerOrders;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in findOrdersForShopOwner: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<CustomerOrder> findById(Long id) {
        return customerOrderRepository.findById(id);
    }

    public void deleteById(Long id) {
        customerOrderRepository.deleteById(id);
    }
    
    public CustomerOrder updateStatus(Long id, String status) {
        Optional<CustomerOrder> orderOpt = customerOrderRepository.findById(id);
        if (orderOpt.isPresent()) {
            CustomerOrder order = orderOpt.get();
            order.setStatus(status);
            return customerOrderRepository.save(order);
        }
        return null;
    }
    
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public CustomerOrder createCompleteOrder(User customer, List<Map<String, Object>> cartItems, 
                                             com.esnaf.yerel_esnaf_siparis.service.ProductService productService,
                                             com.esnaf.yerel_esnaf_siparis.service.OrderItemService orderItemService) {
        System.out.println("DEBUG: createCompleteOrder START - customer: " + customer.getUsername());
        
        // Sipariş oluştur
        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setStatus("Hazırlanıyor");
        order = save(order);
        System.out.println("DEBUG: createCompleteOrder - Created order ID: " + order.getId());
        
        // Flush yap - SQLite için transaction'ı commit et
        customerOrderRepository.flush();
        System.out.println("DEBUG: createCompleteOrder - Flushed order to DB");
        
        // Sipariş kalemlerini oluştur
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map<String, Object> item : cartItems) {
            Long productId = Long.valueOf(item.get("id").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());
            double price = Double.parseDouble(item.get("price").toString());
            
            Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + productId));
            
            System.out.println("DEBUG: createCompleteOrder - Product ID: " + productId + ", Owner ID: " + (product.getOwner() != null ? product.getOwner().getId() : "null"));
            
            // Stok kontrolü
            if (product.getStock() < quantity) {
                deleteById(order.getId());
                throw new RuntimeException(product.getName() + " için yeterli stok yok!");
            }
            
            // Stok güncelle
            product.setStock(product.getStock() - quantity);
            productService.save(product);
            
            // OrderItem oluştur
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPriceSnapshot(price);
            OrderItem savedItem = orderItemService.save(orderItem);
            System.out.println("DEBUG: createCompleteOrder - Saved OrderItem ID: " + savedItem.getId());
            orderItems.add(savedItem);
        }
        
        // Order'ı tekrar kaydet (items ile birlikte)
        order.setItems(orderItems);
        order = save(order);
        System.out.println("DEBUG: createCompleteOrder - Final order ID: " + order.getId() + ", Items count: " + order.getItems().size());
        
        // Flush yap - SQLite için transaction'ı commit et
        customerOrderRepository.flush();
        System.out.println("DEBUG: createCompleteOrder - Flushed final order to DB");
        
        // Doğrulama - Yeni bir transaction'da kontrol et
        Optional<CustomerOrder> verifyOrder = findById(order.getId());
        if (verifyOrder.isPresent()) {
            System.out.println("DEBUG: createCompleteOrder - Order verified in DB! ID: " + verifyOrder.get().getId());
        } else {
            System.out.println("DEBUG: createCompleteOrder - WARNING! Order NOT found in DB after save!");
        }
        
        // Son bir kez daha flush yap
        customerOrderRepository.flush();
        System.out.println("DEBUG: createCompleteOrder - Final flush completed");
        
        return order;
    }
}
