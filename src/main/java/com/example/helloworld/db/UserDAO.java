package com.example.helloworld.db;

import com.example.helloworld.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO<User> {
    public UserDAO(SessionFactory factory) {
        super(factory);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public Optional<User> findByUsername(String username, String password) {
        return Optional.ofNullable(get(username));
    }

    public Optional<User> findOneByUsernameAndPassword(final String username, final String password) {
        Criteria c = criteria()
                .add(Restrictions.eq("username", username))
                .add(Restrictions.eq("password", password));
        User user = (User) c.uniqueResult();
        return Optional.ofNullable(user);
    }

    public User create(User user) {
        return persist(user);
    }

    public List<User> findAll() {
        return list(namedQuery("com.example.helloworld.core.User.findAll"));
    }
}
