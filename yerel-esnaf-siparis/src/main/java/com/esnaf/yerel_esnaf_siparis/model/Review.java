package com.esnaf.yerel_esnaf_siparis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties({"password"})
    private User customer;

    @ManyToOne
    @JsonIgnoreProperties({"products", "reviews"})
    private Product product;

    private int rating; // 1-5 yıldız
    
    @Column(length = 1000)
    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getCustomer() {
        return customer;
    }
    public void setCustomer(User customer) {
        this.customer = customer;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

