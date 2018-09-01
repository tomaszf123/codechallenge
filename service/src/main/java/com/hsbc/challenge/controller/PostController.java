package com.hsbc.challenge.controller;

import com.hsbc.challenge.model.Post;
import com.hsbc.challenge.model.PostRequest;
import com.hsbc.challenge.model.PostResponse;
import com.hsbc.challenge.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@RestController
@RequestMapping(value = "/api/posts", produces = "application/json", consumes = "application/json")
public class PostController {

    private static final Logger LOGGER = Logger.getLogger(PostController.class.getName());

    @Autowired
    private PostRepository postRepository;

    public PostController() {
    }

    @GetMapping
    public ResponseEntity<PostResponse> findAllPosts() {
        LOGGER.info("Got GET request for all posts");
        Iterable<Post> postIterable = postRepository.findAll();
        List<Post> posts = new ArrayList<>();
        postIterable.forEach(posts::add);

        return new ResponseEntity<>(new PostResponse(posts), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findPost(@PathVariable long id) {
        LOGGER.info("Got GET request for id: " + id);
        Optional<Post> post = postRepository.findById(id);
        if (!post.isPresent()) {
            LOGGER.warning("Post not found for: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new PostResponse(post.get()), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> modifyPost(@RequestBody @Valid PostRequest postRequest, @PathVariable long id, BindingResult bindingResult) {
        LOGGER.info("Got PUT request for id: " + id + ", postRequest: " + postRequest);
        if (bindingResult.hasErrors()) {
            LOGGER.warning("Post has incorrect size! Post: " + postRequest.getText());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Post> post = postRepository.findById(id);
        if (!post.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Post dbPost = post.get();
        dbPost.setText(postRequest.getText());
        Post updatedPost = postRepository.save(dbPost);
        return new ResponseEntity<>(new PostResponse(updatedPost), HttpStatus.OK);
    }
}
