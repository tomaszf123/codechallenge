package com.hsbc.challenge.util;


import com.hsbc.challenge.model.Post;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReverseChronologicalPostComparatorTest {

    @Test
    public void shouldCompareInReverseOrder() {
        // given
        Post elderPost = new Post();
        elderPost.setCreationDateTime(createDate(2018, 1,1, 1, 0));
        Post post = new Post();
        post.setCreationDateTime(createDate(2018, 1,1, 1, 10));
        List<Post> posts = Arrays.asList(elderPost, post);

        // when
        Collections.sort(posts, new ReverseChronologicalPostComparator());

        // then
        assertEquals(post, posts.get(0));
        assertEquals(elderPost, posts.get(1));
    }

    private Date createDate(int year, int month, int date, int hourOfDay, int minute) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, date, hourOfDay, minute);
        return instance.getTime();
    }
}