package com.hsbc.challenge.util;

import com.hsbc.challenge.model.Post;

import java.util.Comparator;

public class ReverseChronologicalPostComparator implements Comparator<Post> {

    @Override
    public int compare(Post o1, Post o2) {
        return o2.getCreationDateTime().compareTo(o1.getCreationDateTime());
    }
}
