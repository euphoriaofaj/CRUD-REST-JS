package com.example.CrudApp.controller;

import com.example.CrudApp.model.User;
import com.example.CrudApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String userProfile(Authentication authentication, Model model) {
        System.out.println("=== UserController DEBUG ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Username: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        System.out.println("User found: " + (user != null ? user.getUsername() : "NULL"));

        if (user != null) {
            System.out.println("User roles: " + user.getRoles());
        }

        model.addAttribute("user", user);
        return "user/profile";
    }
}