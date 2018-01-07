package com.example.helloworld;


import com.example.helloworld.auth.BasicAuthenticator;
import com.example.helloworld.auth.BaseAuthorizer;
import com.example.helloworld.auth.OAuthAuthenticator;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.*;
import com.example.helloworld.db.AccessTokenDAO;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.db.UserDAO;
import com.example.helloworld.filter.DateRequiredFeature;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.*;
import com.example.helloworld.tasks.EchoTask;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import jersey.repackaged.com.google.common.collect.Lists;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.List;
import java.util.Map;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
            /* all ORM entities should be registered here */
        new HibernateBundle<HelloWorldConfiguration>(Person.class, AccessToken.class, User.class, Role.class) {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(HelloWorldConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final UserDAO userDao = new UserDAO(hibernateBundle.getSessionFactory());
        final AccessTokenDAO tokenDAO = new AccessTokenDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.admin().addTask(new EchoTask());
        environment.jersey().register(DateRequiredFeature.class);

        /* this is used to associate DAO objects to none resource instances where needed */
        UnitOfWorkAwareProxyFactory unitOfWorkProxyFactory = new UnitOfWorkAwareProxyFactory(hibernateBundle);

        /* associating AccessTokenDAO with OAuthAuthenticator class */
        OAuthAuthenticator oAuthDaoProxy = unitOfWorkProxyFactory.create(OAuthAuthenticator.class, AccessTokenDAO.class, tokenDAO);
        /* setting up caching for oAuth authentication */
        CachingAuthenticator<String, User> oAuthcaching = new CachingAuthenticator<>(
                environment.metrics(), oAuthDaoProxy, configuration.getAuthenticationCacheTime());
        /* Creating oAuth authentication Filter*/
        AuthFilter oauthCredentialAuthFilter = new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(oAuthcaching)
                .setAuthorizer(new BaseAuthorizer())
                .setPrefix("Bearer")
                .buildAuthFilter();


        /* associating user DAO with BasicAuthenticator class */
        BasicAuthenticator basicAuthDaoProxy = unitOfWorkProxyFactory.create(BasicAuthenticator.class, UserDAO.class, userDao);
        /* setting up caching for Basic authentication */
        CachingAuthenticator<BasicCredentials, User> basicAuthCaching = new CachingAuthenticator<>(
                environment.metrics(), basicAuthDaoProxy, configuration.getAuthenticationCacheTime());
        /* Creating Basic authentication Filter*/
        AuthFilter basicCredentialAuthenticationFilter =  new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(basicAuthCaching)
                .setAuthorizer(new BaseAuthorizer())
                .setRealm("Basic")
                .buildAuthFilter();

        /* enabling Basic Authentication or oAuth or both by adding them to the filter chain*/
        //List<AuthFilter> filters = Lists.newArrayList(basicCredentialAuthenticationFilter, oauthCredentialAuthFilter);
        List<AuthFilter> filters = Lists.newArrayList(oauthCredentialAuthFilter);

        environment.jersey().register(new AuthDynamicFeature(new ChainedAuthFilter(filters)));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
        environment.jersey().register(new FilteredResource());
        environment.jersey().register(new OauthTokenEndPoint(userDao, tokenDAO, oAuthcaching));
    }
}
