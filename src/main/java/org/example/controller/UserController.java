package org.example.controller;

import jakarta.validation.constraints.Pattern;
import org.example.pojo.Result;
import org.example.pojo.User;
import org.example.service.UserService;
import org.example.utils.ThreadLocalUtil;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@Pattern(regexp ="^\\S{5,16}$") String username, @Pattern(regexp ="^\\S{5,16}$")  String password){
        userService.register(username,password);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp ="^\\S{5,16}$") String username, @Pattern(regexp ="^\\S{5,16}$")  String password){
        String token=userService.login(username,password);
        return Result.success(token);

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
        Map<String, Object> token = ThreadLocalUtil.get();
        user.setId((Integer) token.get("id"));
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
        String oldPwd = param.get("old_pwd");
        String newPwd = param.get("new_pwd");
        String rePwd = param.get("re_pwd");
        Map<String, Object> claims = ThreadLocalUtil.get();
        String username = (String) claims.get("username");
        userService.updatePwd(username, oldPwd, newPwd, rePwd, token);
        return Result.success();
    }
}
