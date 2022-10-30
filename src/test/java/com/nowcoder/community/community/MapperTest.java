package com.nowcoder.community.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;


@SpringBootTest
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
//        User user = userMapper.selectById(101);
//        System.out.println(user);
        User user2 = userMapper.selectByName("liubei");
        System.out.println(user2);
    }

//    @Test
//    public void testInsertUser(){
//        User user = new User("test","123456","abc","qq.com",1,1, "url","http",new Date());
//        int row = userMapper.insertUser(user);
//        System.out.println(row);
//        System.out.println(user.getId());
//
//    }

    @Test
    public void testupdateUser(){
        int row = userMapper.updatePassword(150,"456789");
        System.out.println(row);
    }

    @Test
    public void testselectPost(){
        for (DiscussPost selectDiscussPost : discussPostMapper.selectDiscussPosts(0,0,10)) {
            System.out.println(selectDiscussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);

    }

}
