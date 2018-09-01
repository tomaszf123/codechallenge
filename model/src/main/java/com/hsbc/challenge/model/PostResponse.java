package com.hsbc.challenge.model;

import java.util.Collections;
import java.util.List;

public class PostResponse {

    private List<Post> posts;

    public PostResponse(Post post) {
        this.posts = Collections.singletonList(post);
    }

    public PostResponse(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
