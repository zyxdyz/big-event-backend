package org.example.service;

import org.example.pojo.User;
import org.hibernate.validator.constraints.URL;

public interface UserService {
    User findUserByName(String username);

    void register(String username, String password);

    void update(User user);

    void updateAvatar(int id,@URL String avatarUrl);

    void updatePwd(int id,String md5String);
}
