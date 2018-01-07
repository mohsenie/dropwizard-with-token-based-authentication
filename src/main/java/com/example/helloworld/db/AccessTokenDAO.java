package com.example.helloworld.db;

import com.example.helloworld.core.AccessToken;
import com.example.helloworld.core.User;
import com.fasterxml.uuid.Generators;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccessTokenDAO extends AbstractDAO<AccessToken>{

    public AccessTokenDAO(SessionFactory factory) {
        super(factory);
    }

    public Optional<AccessToken> findAccessTokenByToken(String token) {
        Criteria c = criteria()
                .add(Restrictions.eq("token", token));
        AccessToken accessToken = (AccessToken) c.uniqueResult();
        return Optional.ofNullable(accessToken);
    }

    public List<AccessToken> findAll() {
        return list(namedQuery("com.example.helloworld.core.AccessToken.findAll"));
    }

    public AccessToken generateNewAccessToken(final User user, final DateTime dateTime)
    {
        UUID uuid = Generators.timeBasedGenerator().generate();
        AccessToken accessToken = user.getAccessToken();
        accessToken.setToken(uuid.toString());
        return persist(accessToken);
    }

    public AccessToken setLastAccessTime(final String token, final DateTime dateTime) {
        Criteria c = criteria()
                .add(Restrictions.eq("token", token));
        AccessToken accessToken = (AccessToken) c.uniqueResult();
        accessToken.setLastAccessUTC(dateTime);
        return persist(accessToken);
    }
}
