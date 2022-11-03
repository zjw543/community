package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

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
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"Account activation",content);
        return map;
        }

        public int activation(int id,String code){
        User user = userMapper.selectById(id);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(id,1);
            return ACTIVATION_SUCCESS;
        }
        else {return ACTIVATION_FAILED;}
        }


        //登录
        public Map<String,Object> login(String username,String password,int expiredSecond){
            Map<String ,Object> map = new HashMap<>();
            //空值处理
            if(StringUtils.isBlank(username)){
                map.put("usernameMsg","Account cant be null!");
                return map;
            } else if (StringUtils.isBlank(username)) {
                map.put("passwordMsg","Password cant be null!");
                return map;
            }
            //验证账号状态
            User user = userMapper.selectByName(username);
            if(user==null){
                map.put("usernameMsg","Account is not exist!");
                return map;
            }
            if(user.getStatus()==0){
                map.put("usernameMsg","Account is not activated!");
                return map;
            }

            //验证密码
            if(!CommunityUtil.md5(user.getPassword()+user.getSalt()).equals(CommunityUtil.md5(password+user.getSalt()))){
                map.put("passwordMsg","Password incorrect!");
                return map;
            };


            //生成登录凭证
            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(user.getId());
            loginTicket.setTicket(CommunityUtil.generateUUID());
            loginTicket.setStatus(0);
            loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*expiredSecond));
            loginTicketMapper.insertLoginTicket(loginTicket);
            map.put("ticket",loginTicket.getTicket());
            return map;

        }

        //退出登录
        public void logout(String ticket){
            loginTicketMapper.updateStatus(ticket,1);
        }
}
