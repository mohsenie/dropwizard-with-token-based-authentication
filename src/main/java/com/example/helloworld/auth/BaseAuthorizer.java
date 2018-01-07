package com.example.helloworld.auth;

import com.example.helloworld.core.Role;
import com.example.helloworld.core.User;
import io.dropwizard.auth.Authorizer;

import java.util.Iterator;

public class BaseAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User user, String role) {

        boolean hasRole = false;
        for (Role itterRole : user.getRoles()) {
            if (itterRole.getName().toLowerCase().contains(role.toLowerCase())) {
                hasRole = true;
                break;
            }
        }
        return hasRole;
    }
}
