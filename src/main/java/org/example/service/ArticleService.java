package org.example.service;

import org.example.pojo.Article;
import org.example.pojo.PageBean;

public interface ArticleService {
    void add(Article article);

    void update(Article article);

    Article detail(Integer id);

    void delete(Integer id);

    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);
}
