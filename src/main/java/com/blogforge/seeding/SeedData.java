package com.blogforge.seeding;

import com.blogforge.entity.Role;
import com.blogforge.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public SeedData (RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(args[0].equalsIgnoreCase("yes")) {
            seedRoles();
        }
    }

    public void seedRoles() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        Role authorRole = new Role();
        authorRole.setName("ROLE_AUTHOR");

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        roleRepository.save(userRole);
        roleRepository.save(authorRole);
        roleRepository.save(adminRole);
    }
    public void seedUsers() {

    }
}
