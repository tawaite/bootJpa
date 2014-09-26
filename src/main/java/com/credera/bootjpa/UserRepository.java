package com.credera.bootjpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findById(long id);
    
    List<User> findByEmail(String email);

}