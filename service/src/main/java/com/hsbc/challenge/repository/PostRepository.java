package com.hsbc.challenge.repository;


import com.hsbc.challenge.model.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends CrudRepository<Post, Long> {

}
