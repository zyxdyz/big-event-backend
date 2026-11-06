package org.example.service.impl;

import org.example.exception.BusinessException;
import org.example.mapper.UserMapper;
import org.example.pojo.User;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public User findUserByName(String username) {
        return userMapper.findUserByName(username);
    }

    @Override
    public void register(String username, String password) {
        if (findUserByName(username) != null) {
            throw new BusinessException("此用户名已被使用");
        }
        //加密
        String m_password = Md5Util.getMD5String(password);
        userMapper.register(username, m_password);
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(int id, String avatarUrl) {
        userMapper.updateAvatar(id, avatarUrl);
    }

    @Override
    public String login(String username, String password) {
        User user = findUserByName(username);
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (!Md5Util.getMD5String(password).equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String token = JwtUtil.genToken(claims);
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(token, token, 12, TimeUnit.HOURS);
        return token;
    }

    @Override
    public void updatePwd(String username, String oldPwd, String newPwd, String rePwd, String token) {
        if (!StringUtils.hasText(oldPwd) || !StringUtils.hasText(newPwd) || !StringUtils.hasText(rePwd)) {
            throw new BusinessException("输入项均不可为空");
        }
        User user = findUserByName(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Md5Util.getMD5String(oldPwd).equals(user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (!newPwd.equals(rePwd)) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (!newPwd.matches("^\\S{5,16}$")) {
            throw new BusinessException("新密码格式不正确");
        }
        userMapper.updatePwd(user.getId(), Md5Util.getMD5String(newPwd));
        //密码更新成功后，使当前令牌立即失效
        stringRedisTemplate.delete(token);
    }
}
