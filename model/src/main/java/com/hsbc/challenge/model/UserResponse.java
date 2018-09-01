package com.hsbc.challenge.model;

import java.util.Collections;
import java.util.List;

public class UserResponse {

    private List<User> users;

    public UserResponse(User user) {
        this.users = Collections.singletonList(user);
    }

    public UserResponse(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
