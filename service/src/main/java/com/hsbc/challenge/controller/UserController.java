package com.hsbc.challenge.controller;

import com.hsbc.challenge.model.*;
import com.hsbc.challenge.repository.UserRepository;
import com.hsbc.challenge.util.ReverseChronologicalPostComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/users", produces = "application/json", consumes = "application/json")
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserRepository userRepository;

    public UserController() {
    }

    @GetMapping
    public ResponseEntity<UserResponse> findAllUsers() {
        LOGGER.info("Got GET request for all users");
        Iterable<User> userIterable = userRepository.findAll();
        List<User> users = new ArrayList<>();
        userIterable.forEach(users::add);
        return new ResponseEntity<>(new UserResponse(users), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUser(@PathVariable long id) {
        LOGGER.info("Got GET request for id: " + id);
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new UserResponse(user.get()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        LOGGER.info("Got POST request to create new user, userRequest: " + userRequest);
        User user = userRepository.save(new User(userRequest));
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(new UserResponse(user), HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/follow/{followeeId}")
    public ResponseEntity<User> followUser(@PathVariable long userId, @PathVariable long followeeId) {
        LOGGER.info("Got POST request - user: " + userId + " follow: " + followeeId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Set<Long> followeeIds = user.get().getFollowees().stream().map(User::getId).collect(Collectors.toSet());
        if (followeeIds.contains(followeeId)) {
            LOGGER.warning("User already follow id: " + followeeId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> followee = userRepository.findById(followeeId);
        if (!followee.isPresent()) {
            LOGGER.warning("Followee not found for id: " + followeeId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.get().getFollowees().add(followee.get());
        userRepository.save(user.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/follow/{followeeId}")
    public ResponseEntity<User> unfollowUser(@PathVariable long userId, @PathVariable long followeeId) {
        LOGGER.info("Got DELETE request - user: " + userId + " follow: " + followeeId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<User> followee = user.get().getFollowees().stream().filter(u -> u.getId() == followeeId).findFirst();
        if (!followee.isPresent()) {
            LOGGER.warning("Followee not found for id: " + followeeId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.get().getFollowees().remove(followee.get());
        userRepository.save(user.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<PostResponse> findUserPosts(@PathVariable long userId) {
        LOGGER.info("Got GET request for all posts userId: " + userId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Post> posts = user.get().getPosts();
        posts.sort(new ReverseChronologicalPostComparator());
        return new ResponseEntity<>(new PostResponse(posts), HttpStatus.OK);
    }

    @GetMapping("/{userId}/timeline")
    public ResponseEntity<PostResponse> findUserFoloweePosts(@PathVariable long userId) {
        LOGGER.info("Got GET timeline request for userId: " + userId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Post> posts = user.get().getFollowees().stream().map(User::getPosts).flatMap(List::stream).sorted(new ReverseChronologicalPostComparator()).collect(Collectors.toList());
        return new ResponseEntity<>(new PostResponse(posts), HttpStatus.OK);
    }

    @GetMapping("/{userId}/posts/{postId}")
    public ResponseEntity<PostResponse> findUserPost(@PathVariable long userId, @PathVariable long postId) {
        LOGGER.info("Got GET request for userId: " + userId + " postId: " + postId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Post> post = user.get().getPosts().stream().filter(p -> p.getId() == postId).findAny();
        if (!post.isPresent()) {
            LOGGER.warning("Post not found for id: " + postId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new PostResponse(post.get()), HttpStatus.OK);
    }

    @PostMapping("/{userId}/posts")
    public ResponseEntity<PostResponse> createUserPost(@PathVariable long userId, @RequestBody @Valid PostRequest postRequest, BindingResult bindingResult) {
        LOGGER.info("Got POST request for userId: " + userId + " post: " + postRequest);
        if (bindingResult.hasErrors()) {
            LOGGER.warning("Post has incorrect size! Post: " + postRequest.getText());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Set<Long> ids = user.get().getPosts().stream().map(Post::getId).collect(Collectors.toSet());
        user.get().getPosts().add(new Post(postRequest));
        User updatedUser = userRepository.save(user.get());
        ids.forEach(id -> updatedUser.getPosts().removeIf(p -> p.getId() == id));
        Optional<Post> post = updatedUser.getPosts().stream().filter(p -> p.getText().equals(postRequest.getText())).findFirst();
        return new ResponseEntity<>(new PostResponse(post.get()), HttpStatus.OK);
    }

    @PutMapping("/{userId}/posts/{postId}")
    public ResponseEntity<PostResponse> modifyUserPost(@PathVariable long userId, @PathVariable long postId, @RequestBody @Valid PostRequest postRequest, BindingResult bindingResult) {
        LOGGER.info("Got PUT request for userId: " + userId + " postId: " + postId + ", postRequest: " + postRequest);
        if (bindingResult.hasErrors()) {
            LOGGER.warning("Post has incorrect size! Post: " + postRequest.getText());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Post> post = user.get().getPosts().stream().filter(p -> p.getId() == postId).findAny();
        if (!post.isPresent()) {
            LOGGER.warning("Post not found for id: " + postId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        post.get().setText(postRequest.getText());
        User updatedUser = userRepository.save(user.get());
        Optional<Post> updatedPost = updatedUser.getPosts().stream().filter(p -> p.getId() == postId).findAny();
        return new ResponseEntity<>(new PostResponse(updatedPost.get()), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/posts/{postId}")
    public ResponseEntity<Post> deleteUserPost(@PathVariable long userId, @PathVariable long postId) {
        LOGGER.info("Got DELETE request for userId: " + userId + " postId: " + postId);
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.warning("User not found for id: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Post> post = user.get().getPosts().stream().filter(p -> p.getId() == postId).findAny();
        if (!post.isPresent()) {
            LOGGER.warning("Post not found for id: " + postId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.get().getPosts().remove(post.get());
        userRepository.save(user.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
