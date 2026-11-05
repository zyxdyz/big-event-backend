package org.example.controller;

import jakarta.validation.constraints.Pattern;
import org.example.pojo.Result;
import org.example.pojo.User;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.Md5Util;
import org.example.utils.ThreadLocalUtil;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/register")
    public Result<String> register(@Pattern(regexp ="^\\S{5,16}$") String username, @Pattern(regexp ="^\\S{5,16}$")  String password){
        User user=userService.findUserByName(username);
        if(user!=null){
            return Result.error("此用户名已被使用");
        }
        else{
            userService.register(username,password);
            return  Result.success();
        }
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp ="^\\S{5,16}$") String username, @Pattern(regexp ="^\\S{5,16}$")  String password){
        User user=userService.findUserByName(username);
        if(user==null){
            return Result.error("用户名不存在");
        }
        if(Md5Util.getMD5String(password).equals(user.getPassword())){
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",user.getId());
            claims.put("username",user.getUsername());
            String token = JwtUtil.genToken(claims);
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,12, TimeUnit.HOURS);
            return Result.success(token);
        }
        else{
            return Result.error("密码错误");
        }
    }

    @GetMapping("/userInfo")
    public Result<User> getUserInfo(){
        Map<String,Object> claims = ThreadLocalUtil.get();
        String username = (String) claims.get("username");
        User user = userService.findUserByName(username);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<String> updateUserInfo(@RequestBody @Validated User user){
        userService.update(user);
        return Result.success();
    }

    @PatchMapping("/updateAvatar")
    public Result<String> updateAvatar(@RequestParam @URL String avatarUrl){
        Map<String,Object> map = ThreadLocalUtil.get();
        int id = (Integer)map.get("id");
        userService.updateAvatar(id,avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result<String> updatePwd(@RequestBody Map<String,String> param,@RequestHeader("Authorization") String token){
        String old_pwd = param.get("old_pwd");
        String new_pwd = param.get("new_pwd");
        String re_pwd = param.get("re_pwd");
        Map<String,Object> map = ThreadLocalUtil.get();
        String username= (String)map.get("username");
        User user= userService.findUserByName(username);
        if(old_pwd.isEmpty()||new_pwd.isEmpty()||re_pwd.isEmpty())
            return Result.error("密码均不可为空");
        if(!user.getPassword().equals(Md5Util.getMD5String(old_pwd))){
            return Result.error("旧密码错误");
        }
        if(!new_pwd.equals(re_pwd))
            return Result.error("两次输入的新密码不一致");
        int id = (Integer)map.get("id");
        userService.updatePwd(id,Md5Util.getMD5String(new_pwd));
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();
    }
}
