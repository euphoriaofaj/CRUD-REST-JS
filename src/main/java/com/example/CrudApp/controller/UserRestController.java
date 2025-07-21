package com.example.CrudApp.controller;

import com.example.CrudApp.model.Role;
import com.example.CrudApp.model.User;
import com.example.CrudApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        System.out.println("=== GET /api/users called ===");
        try {
            List<User> users = userService.getAllUsers();
            System.out.println("Found " + users.size() + " users");
            for (User user : users) {
                System.out.println("User: " + user.getUsername() + ", Roles: " + user.getRoles());
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        System.out.println("=== GET /api/users/" + id + " called ===");
        User user = userService.getUserById(id);
        if (user != null) {
            System.out.println("User found: " + user.getUsername());
            return ResponseEntity.ok(user);
        }
        System.out.println("User not found with ID: " + id);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest userRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== POST /api/users called ===");
            System.out.println("Creating user: " + userRequest.getUsername());
            System.out.println("Password provided: " + (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()));

            if (userRequest.getUsername() == null || userRequest.getUsername().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User existingUser = userService.getUserByUsername(userRequest.getUsername());
            if (existingUser != null) {
                System.out.println("Username already exists: " + userRequest.getUsername());
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (userRequest.getEmail() != null && !userRequest.getEmail().trim().isEmpty()) {
                User existingEmailUser = userService.getUserByEmail(userRequest.getEmail());
                if (existingEmailUser != null) {
                    System.out.println("Email already exists: " + userRequest.getEmail());
                    response.put("success", false);
                    response.put("message", "Email already exists");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            User user = new User();
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setAge(userRequest.getAge());
            user.setEmail(userRequest.getEmail());
            user.setUsername(userRequest.getUsername());
            user.setPassword(userRequest.getPassword());

            System.out.println("Saving user with roles: " + (userRequest.getRoleIds() != null ? String.join(",", userRequest.getRoleIds()) : "null"));
            userService.saveUser(user, userRequest.getRoleIds());

            response.put("success", true);
            response.put("message", "User created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== PUT /api/users/" + id + " called ===");
            User user = userService.getUserById(id);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }

            user.setId(id);
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setAge(userRequest.getAge());
            user.setEmail(userRequest.getEmail());
            user.setUsername(userRequest.getUsername());
            if (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()) {
                user.setPassword(userRequest.getPassword());
            }

            userService.updateUser(user, userRequest.getRoleIds());

            response.put("success", true);
            response.put("message", "User updated successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== DELETE /api/users/" + id + " called ===");
            User user = userService.getUserById(id);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }

            userService.deleteUser(id);

            response.put("success", true);
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        System.out.println("=== GET /api/users/roles called ===");
        List<Role> roles = userService.getAllRoles();
        System.out.println("Found " + roles.size() + " roles");
        return ResponseEntity.ok(roles);
    }

    public static class UserRequest {
        private String firstName;
        private String lastName;
        private Integer age;
        private String email;
        private String username;
        private String password;
        private String[] roleIds;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String[] getRoleIds() { return roleIds; }
        public void setRoleIds(String[] roleIds) { this.roleIds = roleIds; }
    }
}
