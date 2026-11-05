package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.pojo.User;

@Mapper
public interface UserMapper {
    @Select("Select * from user where username=#{username}")
    User findUserByName(String username);

    @Insert("Insert into user(username,password,create_time,update_time)" +
    " Values(#{username},#{mPassword},now(),now())")
    void register(String username, String mPassword);

    @Update("Update user set nickname=#{nickname},email=#{email},update_time=now() where id=#{id}")
    void update(User user);

    @Update("Update user set user_pic=#{avatarUrl},update_time=now() where id=#{id}")
    void updateAvatar(int id, String avatarUrl);

    @Update("Update user set password=#{pwd},update_time=now() where id=#{id}")
    void updatePwd(int id, String pwd);
}
