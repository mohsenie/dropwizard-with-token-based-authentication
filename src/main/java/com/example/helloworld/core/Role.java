package com.example.helloworld.core;

import javax.persistence.*;
import java.security.Principal;
import java.util.Set;



@Entity
@Table(name = "role")
@NamedQueries(
        {
                @NamedQuery(
                        name = "com.example.helloworld.core.Role.findAll",
                        query = "SELECT p FROM Role p"
                )
        })


public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
