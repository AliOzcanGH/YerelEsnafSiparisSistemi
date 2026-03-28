package com.esnaf.yerel_esnaf_siparis.service;

import com.esnaf.yerel_esnaf_siparis.model.Category;
import com.esnaf.yerel_esnaf_siparis.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> findAll() {
        try {
            return categoryRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Hata durumunda boş liste döndür
        }
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category findByName(String name) {
        return categoryRepository.findByName(name);
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}

