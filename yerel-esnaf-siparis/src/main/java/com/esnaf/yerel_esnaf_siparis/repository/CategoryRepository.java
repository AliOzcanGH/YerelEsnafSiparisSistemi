package com.esnaf.yerel_esnaf_siparis.repository;

import com.esnaf.yerel_esnaf_siparis.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}

