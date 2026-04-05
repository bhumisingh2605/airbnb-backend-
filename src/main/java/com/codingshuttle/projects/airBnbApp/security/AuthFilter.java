package com.codingshuttle.projects.airBnbApp.security;


import com.codingshuttle.projects.airBnbApp.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFilter {

    public UserDto signUp() {
         return new UserDto();
    }
}
