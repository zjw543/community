package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.Hostholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Period;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;
    @Autowired
    private Hostholder hostholder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostholder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(1,"请登录后重试");
        }

        followService.follow(user.getId(),entityType,entityId);

        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW)
                .setUserId(hostholder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"关注成功");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostholder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(1,"请登录后重试");
        }

        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }


    //查询关注的人
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        model.addAttribute("user",user);
        page.setPath("/followees/"+userId);
        page.setLimit(5);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        for (Map map:userList){
            User user1 = (User)map.get("user");
            boolean hasFollowed = hasFollowed(user1.getId());
            map.put("hasFollowed",hasFollowed);
        }

        model.addAttribute("users",userList);
        return "/site/followee";
    }

    //查询粉丝
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        model.addAttribute("user",user);
        page.setPath("/followers/"+userId);
        page.setLimit(5);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        for (Map map:userList){
            User user1 = (User)map.get("user");
            boolean hasFollowed = hasFollowed(user1.getId());
            map.put("hasFollowed",hasFollowed);
        }

        model.addAttribute("users",userList);
        return "/site/follower";
    }

    private boolean hasFollowed(int userId){
        if(hostholder.getUser()==null){
            return false;
        }else {
            return followService.hasFollowed(hostholder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
    }

}
