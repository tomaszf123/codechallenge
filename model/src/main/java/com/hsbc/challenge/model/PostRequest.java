package com.hsbc.challenge.model;

import javax.validation.constraints.Size;

public class PostRequest {

    @Size(min = 1, max = 140)
    private String text;

    public PostRequest() {
    }

    public PostRequest(@Size(min = 1, max = 140) String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "PostRequest{" +
                "text='" + text + '\'' +
                '}';
    }
}
