package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;


    //关注
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();
                redisTemplate.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();
                redisTemplate.opsForZSet().remove(followerKey,userId);
                redisTemplate.opsForZSet().remove(followeeKey,entityId);
                return operations.exec();
            }
        });
    }

    //查询关注的实体数量
    public long findFolloweeCount(int userId,int EntityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,EntityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }


    //查询某实体的粉丝数量
    public long fingFollowerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }


    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }


}
