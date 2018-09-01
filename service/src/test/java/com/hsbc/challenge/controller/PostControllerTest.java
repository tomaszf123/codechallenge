package com.hsbc.challenge.controller;

import com.google.gson.Gson;
import com.hsbc.challenge.model.Post;
import com.hsbc.challenge.model.PostRequest;
import com.hsbc.challenge.repository.PostRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PostController.class})
@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PostRepository postRepository;

    @Test
    public void shouldRespondWithStatus200() throws Exception {
        when(postRepository.findAll()).thenReturn(mock(Iterable.class));

        mvc.perform(get("/api/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(0)));

        verify(postRepository, times(1)).findAll();
    }

    @Test
    public void shouldRespondWith404WhenPostDoesNotExists_Get() throws Exception {
        mvc.perform(get("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(postRepository, never()).findAll();
        verify(postRepository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith200AndGetUserPost() throws Exception {
        Post post = createPost(1, "ala ma kota", new Date());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        mvc.perform(get("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].id", is((int) post.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(post.getText())));

        verify(postRepository, never()).findAll();
        verify(postRepository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith400WhenTextIsEmpty() throws Exception {
        PostRequest postRequest = new PostRequest(StringUtils.EMPTY);

        mvc.perform(put("/api/posts/1")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(postRepository, never()).findAll();
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void shouldRespondWith400WhenTextIsTooLong() throws Exception {
        PostRequest postRequest = new PostRequest(StringUtils.repeat("1", 141));

        mvc.perform(put("/api/posts/1")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(postRepository, never()).findAll();
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void shouldRespondWith404WhenPostDoesNotExists_Put() throws Exception {
        PostRequest postRequest = new PostRequest("123");

        mvc.perform(put("/api/posts/1")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(postRepository, never()).findAll();
        verify(postRepository, times(1)).findById(eq(1L));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    public void shouldRespondWith200AndModifyPost() throws Exception {
        String text = "Kota ma Ala";
        Date creationDate = new Date();
        PostRequest postRequest = new PostRequest(text);
        Post post = createPost(1, "ala ma kota", creationDate);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post updatedPost = createPost(1, text, creationDate);
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        mvc.perform(put("/api/posts/1")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].id", is((int) updatedPost.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(text)));

        verify(postRepository, never()).findAll();
        verify(postRepository, times(1)).findById(eq(1L));
        ArgumentCaptor<Post> argumentCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, times(1)).save(argumentCaptor.capture());
        Post savedPost = argumentCaptor.getAllValues().get(0);
        assertEquals(post.getId(), savedPost.getId());
        assertEquals(text, savedPost.getText());
        assertEquals(post.getCreationDateTime(), savedPost.getCreationDateTime());
    }


    private Post createPost(long id, String text, Date creationDate) {
        Post post = new Post();
        post.setId(id);
        post.setText(text);
        post.setCreationDateTime(creationDate);
        return post;
    }
}