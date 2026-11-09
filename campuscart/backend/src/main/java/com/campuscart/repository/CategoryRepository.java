package com.campuscart.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.campuscart.model.Category;

@Repository
public class CategoryRepository {
    private final JdbcTemplate jdbc;

    public CategoryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Category> findAll() {
        return jdbc.query("SELECT id, name FROM categories ORDER BY name",
                (rs, i) -> new Category(rs.getInt("id"), rs.getString("name")));
    }

    public boolean exists(int id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM categories WHERE id = ?", Integer.class, id);
        return n != null && n > 0;
    }
}
