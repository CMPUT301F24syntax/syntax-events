package com.example.syntaxeventlottery;

import java.util.HashSet;
import java.util.Set;

public class RoleManager {
    private Set<Role> roles;
    private UserRepository userRepository;
    private String userId;

    public RoleManager(String userId, UserRepository userRepository) {
        this.roles = new HashSet<>();
        this.userId = userId;
        this.userRepository = userRepository;
    }

    public void addRole(Role role) {
        if (roles.add(role)) {
            userRepository.updateRoles(userId, getRolesString());
        }
    }

    public void deleteRole(Role role) {
        if (roles.remove(role)) {
            userRepository.updateRoles(userId, getRolesString());
        }
    }

    public boolean isEntrant() {
        return roles.contains(Role.ENTRANT);
    }

    public boolean isOrganizer() {
        return roles.contains(Role.ORGANIZER);
    }

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }

    public Set<String> getRolesString() {
        Set<String> rolesString = new HashSet<>();
        for (Role role : roles) {
            rolesString.add(role.name());
        }
        return rolesString;
    }

}
