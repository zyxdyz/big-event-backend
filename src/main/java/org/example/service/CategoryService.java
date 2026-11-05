package org.example.service;

import org.example.pojo.Category;

import java.util.List;

public interface CategoryService {
    void add(Category category);

    void update(Category category);

    List<Category> list();

    Category detail(Integer id);

    void delete(Integer id);
}
