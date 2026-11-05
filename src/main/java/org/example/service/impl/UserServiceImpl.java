package org.example.service.impl;

import org.example.mapper.UserMapper;
import org.example.pojo.User;
import org.example.service.UserService;
import org.example.utils.Md5Util;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User findUserByName(String username) {
        return userMapper.findUserByName(username);
    }

    @Override
    public void register(String username, String password) {
        //加密
        String m_password = Md5Util.getMD5String(password);
        userMapper.register(username,m_password);
    }

    @Override
    public void update(User user) {
        Map<String,Object> token =ThreadLocalUtil.get();
        user.setId((Integer) token.get("id"));
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(int id, String avatarUrl) {
        userMapper.updateAvatar(id,avatarUrl);
    }

    @Override
    public void updatePwd(int id,String pwd) {
        userMapper.updatePwd(id,pwd);
    }
}
