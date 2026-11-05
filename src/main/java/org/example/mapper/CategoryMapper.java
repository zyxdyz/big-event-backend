package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.pojo.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Insert("insert into category(category_name,category_alias,create_time," +
            "update_time,create_user) values(#{categoryName},#{categoryAlias}," +
            "#{createTime},#{updateTime},#{createUser})")
    public void add(Category category);

    @Update("update category set category_name=#{categoryName},category_alias=#{categoryAlias}," +
            "update_time=#{updateTime} where id=#{id}")
    void update(Category category);

    @Select("select * from category where create_user=#{userId}")
    List<Category> list(int userId);

    @Select("select * from category where id=#{id} ")
    Category detail(Integer id);

    @Delete("delete from category where id=#{id}")
    void delete(Integer id);
}
