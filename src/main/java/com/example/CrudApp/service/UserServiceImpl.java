package com.example.CrudApp.service;

import com.example.CrudApp.model.Role;
import com.example.CrudApp.model.User;
import com.example.CrudApp.repository.RoleRepository;
import com.example.CrudApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void saveUser(User user, String[] roleIds) {
        Set<Role> roles = new HashSet<>();
        if (roleIds != null) {
            for (String roleIdStr : roleIds) {
                try {
                    Long roleId = Long.parseLong(roleIdStr);
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        roles.add(role);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user, String[] roleIds) {
        User existingUser = userRepository.findById(user.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setAge(user.getAge());
            existingUser.setEmail(user.getEmail());


            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            if (roleIds != null) {
                Set<Role> roles = new HashSet<>();
                for (String roleIdStr : roleIds) {
                    try {
                        Long roleId = Long.parseLong(roleIdStr);
                        Role role = roleRepository.findById(roleId).orElse(null);
                        if (role != null) {
                            roles.add(role);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid role ID: " + roleIdStr);
                    }
                }
                existingUser.setRoles(roles);
            }


            userRepository.save(existingUser);
        } else {
            System.out.println("User not found with ID: " + user.getId());
        }
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}