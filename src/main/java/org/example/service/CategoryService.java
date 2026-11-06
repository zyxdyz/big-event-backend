package org.example.service;

import org.example.pojo.Category;

import java.util.List;

public interface CategoryService {
    void add(Category category, Integer userId);

    void update(Category category);

    List<Category> list(Integer userId);

    Category detail(Integer id);

    void delete(Integer id);
}
