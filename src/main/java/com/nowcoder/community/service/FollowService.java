package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;


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
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }


    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    //查询某用户关注的人
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(followeeKey,offset,limit+offset-1);
        List<Map<String,Object>> list = new ArrayList<>();
        if(ids!=null){
            for (int id : ids){
                Map map = new HashMap<>();
                map.put("user",userMapper.selectById(id));
                Double score = redisTemplate.opsForZSet().score(followeeKey, id);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
            }
        }
        return list;
    }

    //查询某用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(followerKey,offset,limit+offset-1);
        List<Map<String,Object>> list = new ArrayList<>();
        if(ids!=null){
            for (Integer id : ids){
                Map map = new HashMap<>();
                map.put("user",userMapper.selectById(id));
                Double score = redisTemplate.opsForZSet().score(followerKey, id);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
            }
        }
        return list;
    }

}
