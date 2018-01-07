package com.example.helloworld.resources;

import com.example.helloworld.core.AccessToken;
import com.example.helloworld.core.User;
import com.example.helloworld.db.AccessTokenDAO;
import com.example.helloworld.db.UserDAO;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.caching.CacheControl;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Path("/authenticate")
public class OauthTokenEndPoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(OauthTokenEndPoint.class);

    private final UserDAO userDAO;
    private final AccessTokenDAO accessTokenDAO;
    private final CachingAuthenticator oAuthCaching;

    public OauthTokenEndPoint(UserDAO userDAO, AccessTokenDAO accessTokenDAO, CachingAuthenticator oAuthCaching) {
        this.userDAO = userDAO;
        this.accessTokenDAO = accessTokenDAO;
        this.oAuthCaching = oAuthCaching;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @UnitOfWork
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Optional <AccessToken> authenticate(@FormParam("username") String username, @FormParam("password") String password) {
        LOGGER.info("Login attempt by", username);
        Optional <User> user = this.userDAO.findOneByUsernameAndPassword(username, password);
        if(user.isPresent()) {
           Optional <AccessToken> currentToken = Optional.of(user.get().getAccessToken());
            if(currentToken.isPresent()) {
                //invalidate the exiting token in the cache
                oAuthCaching.invalidate(currentToken.get().getToken());
            }
            AccessToken newAccessToken = accessTokenDAO.generateNewAccessToken(user.get(), new DateTime());
            return Optional.ofNullable(newAccessToken);
        }
        return Optional.empty();
    }
}

