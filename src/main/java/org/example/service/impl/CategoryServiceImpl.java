package org.example.service.impl;

import org.example.mapper.CategoryMapper;
import org.example.pojo.Category;
import org.example.service.CategoryService;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public void add(Category category) {
        Map<String,Object> map = ThreadLocalUtil.get();
        int id =(Integer)map.get("id");
        category.setCreateUser(id);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.add(category);
    }

    @Override
    public void update(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    @Override
    public List<Category> list() {
        Map<String,Object> map = ThreadLocalUtil.get();
        int userId =(Integer)map.get("id");
        return categoryMapper.list(userId);
    }

    @Override
    public Category detail(Integer id) {
        return categoryMapper.detail(id);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }
}
