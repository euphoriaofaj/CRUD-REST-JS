package com.example.CrudApp.service;
import com.example.CrudApp.model.User;
import com.example.CrudApp.model.Role;
import com.example.CrudApp.dto.UserRequest;
import java.util.Map;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    void saveUser(User user);
    void saveUser(User user, String[] roleNames);
    void updateUser(User user, String[] roleNames);
    void deleteUser(Long id);
    List<Role> getAllRoles();

    Map<String, Object> createUser(UserRequest userRequest);
    Map<String, Object> updateUser(Long id, UserRequest userRequest);
    Map<String, Object> deleteUserById(Long id);
}