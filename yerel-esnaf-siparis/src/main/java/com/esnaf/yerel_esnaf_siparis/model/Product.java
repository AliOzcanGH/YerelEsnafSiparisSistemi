package com.esnaf.yerel_esnaf_siparis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stock;
    
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"password", "email", "phone", "address", "fullName", "role"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User owner;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"products"})
    private Category category;
    
    // JSON için yardımcı getter'lar
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }
    
    public String getOwnerName() {
        return owner != null ? owner.getUsername() : null;
    }
    
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
    
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}

