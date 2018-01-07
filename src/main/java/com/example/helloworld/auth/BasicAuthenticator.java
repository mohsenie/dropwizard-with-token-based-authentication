package com.example.helloworld.auth;

import com.example.helloworld.core.User;
import com.example.helloworld.db.UserDAO;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.Optional;

public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
    /**
     * Valid users with mapping user -> roles
     */

    private UserDAO userDAO;

    public BasicAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @UnitOfWork
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        Optional<User> user = userDAO.findOneByUsernameAndPassword(credentials.getUsername(), credentials.getPassword());
        return user;
    }
}
