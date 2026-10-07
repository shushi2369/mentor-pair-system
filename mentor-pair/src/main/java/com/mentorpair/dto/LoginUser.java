package com.mentorpair.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginUser implements Serializable {

    public static final String SESSION_KEY = "LOGIN_USER";

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_MENTOR = 2;
    public static final int ROLE_STUDENT = 3;

    private Long id;
    private String username;
    private String realName;
    private Integer role;

    public boolean isAdmin() {
        return role != null && role == ROLE_ADMIN;
    }

    public boolean isMentor() {
        return role != null && role == ROLE_MENTOR;
    }

    public boolean isStudent() {
        return role != null && role == ROLE_STUDENT;
    }
}
