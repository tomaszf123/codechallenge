package com.hsbc.challenge.model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String username;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Post> posts;

    @OneToMany(orphanRemoval = false, cascade = CascadeType.PERSIST)
    private Set<User> followees;

    public User() {
    }

    public User(UserRequest userRequest) {
        this.username = userRequest.getUsername();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public Set<User> getFollowees() {
        return followees;
    }

    public void setFollowees(Set<User> followees) {
        this.followees = followees;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", posts=" + posts +
                ", followees=" + followees +
                '}';
    }
}
