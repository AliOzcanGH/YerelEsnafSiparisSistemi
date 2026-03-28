package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.CustomerOrder;
import com.esnaf.yerel_esnaf_siparis.model.Product;
import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProductService productService;
    @Autowired
    private CustomerOrderService customerOrderService;

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return; // Kullanıcı zaten yok
        }
        
        User user = userOpt.get();
        
        // 1. Kullanıcıya ait tüm ürünleri sil
        List<Product> userProducts = productService.findByOwner(user);
        for (Product product : userProducts) {
            productService.deleteById(product.getId());
        }
        
        // 2. Kullanıcıya ait tüm siparişleri sil (OrderItem'lar cascade ile silinecek)
        List<CustomerOrder> userOrders = customerOrderService.findByCustomer(user);
        for (CustomerOrder order : userOrders) {
            customerOrderService.deleteById(order.getId());
        }
        
        // 3. Son olarak kullanıcıyı sil
        userRepository.deleteById(id);
    }
}
