package com.campuscart.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campuscart.model.Category;
import com.campuscart.repository.CategoryRepository;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categories;

    public CategoryController(CategoryRepository categories) { this.categories = categories; }

    @GetMapping
    public List<Category> all() { return categories.findAll(); }
}
