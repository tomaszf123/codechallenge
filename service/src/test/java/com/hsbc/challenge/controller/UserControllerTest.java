package com.hsbc.challenge.controller;

import com.google.gson.Gson;
import com.hsbc.challenge.model.Post;
import com.hsbc.challenge.model.PostRequest;
import com.hsbc.challenge.model.User;
import com.hsbc.challenge.model.UserRequest;
import com.hsbc.challenge.repository.UserRepository;
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

import java.util.*;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserController.class})
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository repository;

    @Test
    public void shouldRespondWith200WhenUsersDoNotExist() throws Exception {
        when(repository.findAll()).thenReturn(mock(Iterable.class));

        mvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(0)));

        verify(repository, times(1)).findAll();
    }

    @Test
    public void shouldRespondWith200AndAllUsers() throws Exception {
        User user1 = createUser(1, "user1", Collections.emptyList(), Collections.emptySet());
        User user2 = createUser(2, "user2", Collections.emptyList(), Collections.emptySet());
        when(repository.findAll()).thenReturn(Arrays.asList(user1, user2));

        mvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.users[0].id", is((int) user1.getId())))
                .andExpect(jsonPath("$.users[1].id", is((int) user2.getId())))
                .andExpect(jsonPath("$.users[0].username", is(user1.getUsername())))
                .andExpect(jsonPath("$.users[1].username", is(user2.getUsername())));

        verify(repository, times(1)).findAll();
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_Get() throws Exception {
        mvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith200AndUserDetails() throws Exception {
        User user = createUser(1, "username", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].id", is((int) user.getId())))
                .andExpect(jsonPath("$.users[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$.users[0].followees", hasSize(0)));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith500WhenUserNotCreated() throws Exception {
        mvc.perform(post("/api/users")
                .content(new Gson().toJson(new UserRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(StringUtils.EMPTY));
    }

    @Test
    public void shouldRespondWith301WhenUserCreated() throws Exception {
        String username = "username1";
        UserRequest userRequest = new UserRequest(username);
        User user = createUser(1, username, Collections.emptyList(), Collections.emptySet());
        when(repository.save(any())).thenReturn(user);

        mvc.perform(post("/api/users")
                .content(new Gson().toJson(userRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].id", is((int) user.getId())))
                .andExpect(jsonPath("$.users[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$.users[0].followees", hasSize(0)));

        ArgumentCaptor<User> requestCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(requestCaptor.capture());
        User savedUser = requestCaptor.getAllValues().get(0);
        assertEquals(userRequest.getUsername(), savedUser.getUsername());
    }

    @Test
    public void shouldRespondWith404WhenUserNotFound_Follow() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(post("/api/users/5/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(5L));
        verify(repository, never()).findById(eq(2L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith404WhenFolloweeNotFound_Follow() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(post("/api/users/1/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, times(1)).findById(eq(2L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith200AndAddFoloweeToUser() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), new HashSet<>());
        User followee = createUser(2, "username2", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findById(2L)).thenReturn(Optional.of(followee));

        mvc.perform(post("/api/users/1/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, times(1)).findById(eq(2L));

        ArgumentCaptor<User> requestCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(requestCaptor.capture());
        User savedUser = requestCaptor.getAllValues().get(0);
        assertEquals(1, user.getFollowees().size());
        assertTrue(user.getFollowees().contains(followee));
    }

    @Test
    public void shouldRespondWith400WhenUserIsAlreadyFollowed() throws Exception {
        User followee = createUser(2, "username2", Collections.emptyList(), Collections.emptySet());
        User user = createUser(1, "username1", Collections.emptyList(), new HashSet<>(Collections.singleton(followee)));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(post("/api/users/1/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).findById(eq(2L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith404WhenUserNotFound_Unfollow() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(delete("/api/users/5/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(5L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith404WhenFolloweeNotFound_Unfollow() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(delete("/api/users/1/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith200AndRemoveFolloweeFromUser() throws Exception {
        User followee = createUser(2, "username2", Collections.emptyList(), Collections.emptySet());
        User user = createUser(1, "username1", Collections.emptyList(), new HashSet<>(Collections.singleton(followee)));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(delete("/api/users/1/follow/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(repository, times(1)).findById(eq(1L));

        ArgumentCaptor<User> requestCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(requestCaptor.capture());
        User savedUser = requestCaptor.getAllValues().get(0);
        assertTrue(savedUser.getFollowees().isEmpty());
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_Wall() throws Exception {
        mvc.perform(get("/api/users/1/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith200AndAllUserPostsWithReverseChronologicalOrder() throws Exception {
        Post post1 = createPost(5, "ala", createDate(5, 10, 20));
        Post post2 = createPost(15, "ala", createDate(1, 3, 3));
        Post post3 = createPost(25, "ala", createDate(5, 10, 1));
        User user = createUser(1, "user", Arrays.asList(post1, post2, post3), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/1/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(3)))
                .andExpect(jsonPath("$.posts[0].id", is((int) post1.getId())))
                .andExpect(jsonPath("$.posts[1].id", is((int) post3.getId())))
                .andExpect(jsonPath("$.posts[2].id", is((int) post2.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(post1.getText())))
                .andExpect(jsonPath("$.posts[1].text", is(post3.getText())))
                .andExpect(jsonPath("$.posts[2].text", is(post2.getText())));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_Timeline() throws Exception {
        mvc.perform(get("/api/users/1/timeline")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith200AndAllFoloweePostsWithReverseChronologicalOrder() throws Exception {
        Post post1 = createPost(6, "ala", createDate(5, 10, 20));
        Post post2 = createPost(7, "ala", createDate(1, 3, 3));
        Post post3 = createPost(8, "ala", createDate(5, 10, 1));
        Post post4 = createPost(10, "ala", createDate(6, 11, 2));
        User followee1 = createUser(2, "username2", Arrays.asList(post1, post3), Collections.emptySet());
        User followee2 = createUser(3, "username3", Collections.singletonList(post2), Collections.emptySet());
        User followee3 = createUser(4, "username4", Collections.singletonList(post4), Collections.emptySet());

        User user = createUser(1, "user", Collections.singletonList(createPost(10, "x", new Date())),
                new HashSet<>(Arrays.asList(followee1, followee2, followee3)));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/1/timeline")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(4)))
                .andExpect(jsonPath("$.posts[0].id", is((int) post4.getId())))
                .andExpect(jsonPath("$.posts[1].id", is((int) post1.getId())))
                .andExpect(jsonPath("$.posts[2].id", is((int) post3.getId())))
                .andExpect(jsonPath("$.posts[3].id", is((int) post2.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(post4.getText())))
                .andExpect(jsonPath("$.posts[1].text", is(post1.getText())))
                .andExpect(jsonPath("$.posts[2].text", is(post3.getText())))
                .andExpect(jsonPath("$.posts[3].text", is(post2.getText())));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_SinglePost_Get() throws Exception {
        mvc.perform(get("/api/users/1/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith404WhenPostDoesNotExists_SinglePost_Get() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/1/posts/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
    }

    @Test
    public void shouldRespondWith200AndGetUserPost() throws Exception {
        Post post1 = createPost(1, "Ala ma kota1");
        Post post2 = createPost(2, "Ala ma kota2");
        User user = createUser(10, "username1", Arrays.asList(post2, post1), Collections.emptySet());
        when(repository.findById(10L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/10/posts/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].id", is((int) post2.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(post2.getText())));

        verify(repository, times(1)).findById(eq(10L));
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_SinglePost_Post() throws Exception {
        PostRequest postRequest = new PostRequest("xxx");

        mvc.perform(post("/api/users/1/posts")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith400WhenPostTextIsEmpty() throws Exception {
        PostRequest postRequest = new PostRequest(StringUtils.EMPTY);

        mvc.perform(post("/api/users/1/posts")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, never()).findById(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith400WhenPostTextIsTooLong() throws Exception {
        PostRequest postRequest = new PostRequest(StringUtils.repeat("x", 141));

        mvc.perform(post("/api/users/1/posts")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, never()).findById(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith200AndAddPostToUser() throws Exception {
        final String postText = "Ala ma kota";
        Post post = createPost(1, postText);
        PostRequest postRequest = new PostRequest(postText);

        User user = createUser(1, "username1", new ArrayList<>(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        User updatedUser = createUser(1, "username1", Collections.singletonList(post), Collections.emptySet());
        when(repository.save(any())).thenReturn(updatedUser);

        mvc.perform(post("/api/users/1/posts")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].id", is((int) post.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(post.getText())));

        verify(repository, times(1)).findById(eq(1L));
        ArgumentCaptor<User> requestCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(requestCaptor.capture());
        User savedUser = requestCaptor.getAllValues().get(0);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(1, savedUser.getPosts().size());
        assertEquals(postText, savedUser.getPosts().get(0).getText());
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_SinglePost_Put() throws Exception {
        PostRequest postRequest = new PostRequest("xxx");

        mvc.perform(put("/api/users/1/posts/1")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith404WhenPostDoesNotExists_SinglePost_Put() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/1/posts/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith200AndModifyPost() throws Exception {
        final String updatedText = "Ala ma kota";
        Post post = createPost(1, "X");
        PostRequest postRequest = new PostRequest(updatedText);
        User user = createUser(1, "username1", Collections.singletonList(post), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        Post updatedPost = createPost(1, updatedText);
        User updatedUser = createUser(1, "username1", Collections.singletonList(updatedPost), Collections.emptySet());
        when(repository.save(user)).thenReturn(updatedUser);

        mvc.perform(put("/api/users/1/posts/1")
                .content(new Gson().toJson(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].id", is((int) updatedPost.getId())))
                .andExpect(jsonPath("$.posts[0].text", is(updatedPost.getText())));

        verify(repository, times(1)).findById(eq(1L));
        ArgumentCaptor<User> requestCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(requestCaptor.capture());
        User savedUser = requestCaptor.getAllValues().get(0);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getPosts().size(), savedUser.getPosts().size());
        assertEquals(user.getPosts().get(0).getId(), savedUser.getPosts().get(0).getId());
        assertEquals(user.getPosts().get(0).getText(), savedUser.getPosts().get(0).getText());
    }

    @Test
    public void shouldRespondWith404WhenUserDoesNotExists_SinglePost_Delete() throws Exception {
        mvc.perform(delete("/api/users/1/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith404WhenPostDoesNotExists_SinglePost_Delete() throws Exception {
        User user = createUser(1, "username1", Collections.emptyList(), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(delete("/api/users/1/posts/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringUtils.EMPTY));

        verify(repository, times(1)).findById(eq(1L));
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldRespondWith200AndRemoveUserPost() throws Exception {
        Post post = createPost(1, "x");
        User user = createUser(1, "username1", new ArrayList<>(Collections.singletonList(post)), Collections.emptySet());
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        mvc.perform(delete("/api/users/1/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(repository, times(1)).findById(eq(1L));
        ArgumentCaptor<User> requestCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(requestCaptor.capture());
        User savedUser = requestCaptor.getAllValues().get(0);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertTrue(savedUser.getPosts().isEmpty());

    }

    private User createUser(long id, String username, List<Post> posts, Set<User> followees) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPosts(posts);
        user.setFollowees(followees);
        return user;
    }

    private Date createDate(int date, int hourOfDay, int minute) {
        Calendar instance = Calendar.getInstance();
        instance.set(2018, 10, date, hourOfDay, minute);
        return instance.getTime();
    }

    private Post createPost(long id, String text) {
        return createPost(id, text, null);
    }

    private Post createPost(long id, String text, Date creationDate) {
        Post post = new Post();
        post.setId(id);
        post.setText(text);
        post.setCreationDateTime(creationDate);
        return post;
    }
}