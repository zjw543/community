package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.Hostholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.awt.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private Hostholder hostholder;

    @Autowired
    private LikeService likeService;


    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId,int entityUserId){
        User user = hostholder.getUser();
        int userId = user.getId();
        //点赞
        likeService.like(userId,entityType,entityId,entityUserId);

        //查询点赞状态
        int likeStatus = likeService.findEntityLikeStatus(userId, entityType, entityId);

        //查询点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeStatus",likeStatus);
        map.put("likeCount",likeCount);
        return CommunityUtil.getJSONString(0,null,map);
    }


}