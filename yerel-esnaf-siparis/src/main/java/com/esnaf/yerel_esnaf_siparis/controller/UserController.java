package com.esnaf.yerel_esnaf_siparis.controller;

import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping("/by-username/{username}")
    public User getByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }
}
