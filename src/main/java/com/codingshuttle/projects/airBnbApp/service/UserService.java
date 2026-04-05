package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.entity.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User createUser(User user);

    User getUserById(Long id);
}
