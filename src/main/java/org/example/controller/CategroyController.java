package org.example.controller;

import org.example.pojo.Category;
import org.example.pojo.Result;
import org.example.service.CategoryService;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
public class CategroyController {
    @Autowired
    CategoryService categoryService;

    private Integer getUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        return (Integer) claims.get("id");
    }

    @PostMapping
    public Result<String> add(@RequestBody @Validated Category category){
        categoryService.add(category, getUserId());
        return Result.success();
    }

    @PutMapping
    public Result<String> update(@RequestBody @Validated(Category.Update.class) Category category){
        categoryService.update(category);
        return Result.success();
    }

    @GetMapping
    public Result<List<Category>> list(){
        List<Category> list = categoryService.list(getUserId());
        return Result.success(list);
    }

    @GetMapping("/detail")
    public Result<Category> detail(Integer id){
        Category category = categoryService.detail(id);
        return Result.success(category);
    }

    @DeleteMapping
    public Result<String> delete(Integer id){
        categoryService.delete(id);
        return Result.success();
    }
}
