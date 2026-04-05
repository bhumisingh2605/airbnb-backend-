package com.codingshuttle.projects.airBnbApp.dto;


import com.codingshuttle.projects.airBnbApp.entity.User;
import lombok.Data;

@Data
public class SignUpRequestDto {

    private String email;
    private String password;
    private String name;


}
