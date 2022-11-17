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

import javax.lang.model.element.NestingKind;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void  like (int userId,int entityType,int entityId,int entityUserId){
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
//        if(isMember){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else {
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }

        //编程式
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                //发帖人收到的点赞的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询写在事务之外
                //是否点过赞
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);

                //开启事务
                operations.multi();
                //点赞
                if(isMember){
                    //点过赞
                    //取消点赞，移除点赞集合中当前用户的id
                    operations.opsForSet().remove(entityLikeKey,userId);
                    //发帖人的总赞数量减一
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    //没点过赞
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                //被点赞人所有的赞
                return operations.exec();
            }

        });
    }

    //查询某实体的点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }
    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)? 1 : 0;
    }

    //查询用户获得的赞的总数
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer likeCount = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return likeCount == null ? 0 : likeCount.intValue();
    }

}
