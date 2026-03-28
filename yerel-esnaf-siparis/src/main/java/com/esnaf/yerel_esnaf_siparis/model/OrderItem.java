package com.esnaf.yerel_esnaf_siparis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"owner", "category"})
    private Product product;

    private int quantity;
    private double priceSnapshot;

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public CustomerOrder getOrder() {
        return order;
    }
    public void setOrder(CustomerOrder order) {
        this.order = order;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public double getPriceSnapshot() {
        return priceSnapshot;
    }
    public void setPriceSnapshot(double priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }
}

