package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }


    //2.找不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }


    //3.数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }




    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
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
            clearCache(id);
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
            if(!(user.getPassword().equals(CommunityUtil.md5(password+user.getSalt())))){
                map.put("passwordMsg","Password incorrect!");
                return map;
            };


            //生成登录凭证
            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(user.getId());
            loginTicket.setTicket(CommunityUtil.generateUUID());
            loginTicket.setStatus(0);
            loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*expiredSecond));



//            loginTicketMapper.insertLoginTicket(loginTicket);
            String ticketKey = RedisKeyUtil.getTicketkey(loginTicket.getTicket());
            redisTemplate.opsForValue().set(ticketKey,loginTicket);

            map.put("ticket",loginTicket.getTicket());
            return map;

        }

        //退出登录
        public void logout(String ticket){
            String ticketKey = RedisKeyUtil.getTicketkey(ticket);
            LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(ticketKey);
            loginTicket.setStatus(1);
            redisTemplate.opsForValue().set(ticketKey,loginTicket);
        }

        //查询登录凭证
        public LoginTicket findLoginTicket(String ticket){
            String ticketKey = RedisKeyUtil.getTicketkey(ticket);
            LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(ticketKey);
            return loginTicket;
        }

        //更新头像链接
        public int updateHeader(int id,String url){
            int rows = userMapper.updateHeader(id,url);
            clearCache(id);
            return rows;
        }

        //修改密码
        public Map<String,Object> updatePassword(int userId,String oldPassword,String newPassword){
            Map<String,Object> map = new HashMap<>();
            //判断旧密码是否正确
            User user = userMapper.selectById(userId);
            if (!(user.getPassword().equals(CommunityUtil.md5(oldPassword+user.getSalt())))){
                //旧密码错误
                map.put("passwordMsg","密码错误！");
                return map;
            }
            //判断密码是否合规
            if(StringUtils.isBlank(newPassword)){
                map.put("newPasswordMsg","密码不能为空！");
                return map;
            }
            if (StringUtils.length(newPassword)<8){
                map.put("newPasswordMsg","密码至少为8位！");
                return map;
            }
            userMapper.updatePassword(userId,CommunityUtil.md5(newPassword+user.getSalt()));
            clearCache(userId);
            //修改成功map为空
            return map;
        }

}
