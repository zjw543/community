package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public User findUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }




    //注册
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(user==null){
            throw new IllegalArgumentException("Params cant be null!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","username cant be null");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "password cant be null");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "email cant be null");
            return map;
        }

        //验证账号
        User user1 = userMapper.selectByName(user.getUsername());
        if(user1!=null){
            map.put("usernameMsg", "This username is already exist");
            return map;
        }
        user1 = userMapper.selectByEmail(user.getEmail());
            if(user1!=null){
                map.put("emailMsg", "This email is already exist");
                return map;
            }

        //注册用户
        //密码加密
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        //设置其他属性
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //给用户发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"Account activation",content);

        return map;
        }
}
