package com.imooc.user.service;

import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@RequestMapping("user-api")
@FeignClient("foodie-user-service")
public interface UserService {

    /**
     * 判断用户名是否存在
     */
    @GetMapping("user/exist")
    public boolean queryUsernameIsExist(@RequestParam("username") String username);

    /**
     * 判断用户名是否存在
     */
    @PostMapping("user")
    public Users createUser(@RequestBody UserBO userBO);

    /**
     * 检索用户名和密码是否匹配，用于登录
     */
    @GetMapping("verify")
    public Users queryUserForLogin(@RequestParam("username") String username,
                                   @RequestParam("password") String password);
}
