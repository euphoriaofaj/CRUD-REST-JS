package com.example.CrudApp.service;

import com.example.CrudApp.model.Role;
import com.example.CrudApp.model.User;
import com.example.CrudApp.dto.UserRequest;
import com.example.CrudApp.repository.RoleRepository;
import com.example.CrudApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
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
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.getRoles().size());
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.getRoles().size();
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.getRoles().size();
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.getRoles().size();
        }
        return user;
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void saveUser(User user, String[] roleIds) {
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        Set<Role> roles = new HashSet<>();
        if (roleIds != null) {
            for (String roleIdStr : roleIds) {
                try {
                    Long roleId = Long.parseLong(roleIdStr);
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        roles.add(role);
                    }
                } catch (NumberFormatException ignored) {}
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
                    } catch (NumberFormatException ignored) {}
                }
                existingUser.setRoles(roles);
            }

            userRepository.save(existingUser);
        }
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Map<String, Object> createUser(UserRequest userRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userRequest.getUsername() == null || userRequest.getUsername().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                return response;
            }

            if (userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required");
                return response;
            }

            User existingUser = userRepository.findByUsername(userRequest.getUsername());
            if (existingUser != null) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return response;
            }

            if (userRequest.getEmail() != null && !userRequest.getEmail().trim().isEmpty()) {
                User existingEmailUser = userRepository.findByEmail(userRequest.getEmail());
                if (existingEmailUser != null) {
                    response.put("success", false);
                    response.put("message", "Email already exists");
                    return response;
                }
            }

            User user = new User();
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setAge(userRequest.getAge());
            user.setEmail(userRequest.getEmail());
            user.setUsername(userRequest.getUsername());
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

            Set<Role> roles = new HashSet<>();
            if (userRequest.getRoleIds() != null) {
                for (String roleIdStr : userRequest.getRoleIds()) {
                    try {
                        Long roleId = Long.parseLong(roleIdStr);
                        Role role = roleRepository.findById(roleId).orElse(null);
                        if (role != null) {
                            roles.add(role);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            user.setRoles(roles);

            userRepository.save(user);

            response.put("success", true);
            response.put("message", "User created successfully");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating user: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> updateUser(Long id, UserRequest userRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setAge(userRequest.getAge());
            user.setEmail(userRequest.getEmail());

            if (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            }

            Set<Role> roles = new HashSet<>();
            if (userRequest.getRoleIds() != null) {
                for (String roleIdStr : userRequest.getRoleIds()) {
                    try {
                        Long roleId = Long.parseLong(roleIdStr);
                        Role role = roleRepository.findById(roleId).orElse(null);
                        if (role != null) {
                            roles.add(role);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            user.setRoles(roles);

            userRepository.save(user);

            response.put("success", true);
            response.put("message", "User updated successfully");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating user: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> deleteUserById(Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            userRepository.deleteById(id);

            response.put("success", true);
            response.put("message", "User deleted successfully");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting user: " + e.getMessage());
            return response;
        }
    }
}
