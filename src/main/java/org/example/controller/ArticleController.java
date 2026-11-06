package org.example.controller;

import org.apache.ibatis.annotations.Delete;
import org.example.pojo.Article;
import org.example.pojo.PageBean;
import org.example.pojo.Result;
import org.example.service.ArticleService;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    private Integer getUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        return (Integer) claims.get("id");
    }

    @PostMapping
    public Result<String> add(@RequestBody @Validated Article article){
        articleService.add(article, getUserId());
        return Result.success();
    }

    @PutMapping
    public Result<String> update(@RequestBody @Validated(Article.Update.class) Article article){
        articleService.update(article);
        return Result.success();
    }

    @GetMapping("/detail")
    public Result<Article> detail(@RequestParam Integer id){
        Article article = articleService.detail(id);
        return Result.success(article);
    }

    @DeleteMapping
    public Result<String> delete(@RequestParam Integer id){
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping
    public Result<PageBean<Article>> list(Integer pageNum, Integer pageSize,
                                 @RequestParam(required = false) Integer categoryId,
                                 @RequestParam(required = false) String state){
        PageBean<Article> p = articleService.list(pageNum,pageSize,categoryId,state, getUserId());
        return Result.success(p);
    }
}
