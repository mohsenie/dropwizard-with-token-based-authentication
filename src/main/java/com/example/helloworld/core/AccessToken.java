package com.example.helloworld.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "access_token")
@NamedQueries(
        {
                @NamedQuery(
                        name = "com.example.helloworld.core.AccessToken.findAll",
                        query = "SELECT p FROM AccessToken p"
                )
        })

public class AccessToken {

    //all entities should have ID filed
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore // ignore this field when rendering the json response
    private long id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "datetime", nullable = false)
    private DateTime lastAccessUTC;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public AccessToken(){
    }

    public AccessToken(String token, User user, DateTime lastAccessUTC) {
        this.token = token;
        this.user = user;
        this.lastAccessUTC = lastAccessUTC;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DateTime getLastAccessUTC() {
        return lastAccessUTC;
    }

    public void setLastAccessUTC(DateTime lastAccessUTC) {
        this.lastAccessUTC = lastAccessUTC;
    }
}
