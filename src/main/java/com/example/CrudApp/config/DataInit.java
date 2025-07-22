package com.example.CrudApp.config;

import com.example.CrudApp.model.Role;
import com.example.CrudApp.model.User;
import com.example.CrudApp.repository.RoleRepository;
import com.example.CrudApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInit implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN");
        Role userRole = roleRepository.findByName("USER");

        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole = roleRepository.save(adminRole);
        }

        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            userRole = roleRepository.save(userRole);
        }

        User existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@example.com");

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
        } else {
            String encodedPassword = passwordEncoder.encode("admin");
            if (!passwordEncoder.matches("admin", existingAdmin.getPassword())) {
                existingAdmin.setPassword(encodedPassword);
                userRepository.save(existingAdmin);
            }
        }
    }
}
