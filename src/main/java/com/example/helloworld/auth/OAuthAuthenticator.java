package com.example.helloworld.auth;

import com.example.helloworld.core.AccessToken;
import com.example.helloworld.core.User;
import com.example.helloworld.db.AccessTokenDAO;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.hibernate.UnitOfWork;
import org.joda.time.DateTime;
import org.joda.time.Period;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class OAuthAuthenticator implements Authenticator<String, User> {

    private static final int ACCESS_TOKEN_EXPIRE_TIME_MIN = 30;
    private AccessTokenDAO accessTokenDAO;

    public OAuthAuthenticator(AccessTokenDAO accessTokenDAO) {
        this.accessTokenDAO = accessTokenDAO;
    }

    @Override
    @UnitOfWork
    public Optional<User> authenticate(String token) throws AuthenticationException {

        if(token == null){
            return Optional.empty();
        }

        // Get the access token from the database
        Optional<AccessToken> accessToken = accessTokenDAO.findAccessTokenByToken(token);
        if (!accessToken.isPresent()) {
            return Optional.empty();
        }

        // Check if the last access time is not too far in the past (the access token is expired)
        Period period = new Period(accessToken.get().getLastAccessUTC(), new DateTime());
        if (period.getMinutes() > ACCESS_TOKEN_EXPIRE_TIME_MIN) {
            return Optional.empty();
        }

        // Update the access time for the token
        accessTokenDAO.setLastAccessTime(token, new DateTime());

        // Return the user's id for processing
        return Optional.ofNullable(accessToken.get().getUser());
    }
}
