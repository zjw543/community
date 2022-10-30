package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

//    public User() {
//    }
//
//    public User(String username, String password, String salt, String email, int type, int status,
//                String activationCode, String headerUrl, Date createTime) {
//        this.id = id;
//        this.username = username;
//        this.password = password;
//        this.salt = salt;
//        this.email = email;
//        this.type = type;
//        this.status = status;
//        this.activationCode = activationCode;
//        this.headerUrl = headerUrl;
//        this.createTime = createTime;
//    }
}
