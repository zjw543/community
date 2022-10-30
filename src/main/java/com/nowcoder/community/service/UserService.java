package com.nowcoder.community.service;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public User findUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }


}
