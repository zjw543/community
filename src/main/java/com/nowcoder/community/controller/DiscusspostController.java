package com.nowcoder.community.controller;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
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

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscusspostController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private Hostholder hostholder;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostholder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"You are not login yet.");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        //错误统一处理
        return CommunityUtil.getJSONString(0,"DiscussPost release successful");
    }

    @RequestMapping(path ="/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId")int id, Model model,Page page){

        //帖子内容
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post",post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //评论内容
        //Page page = new Page();
        page.setLimit(5);
        page.setRows(post.getCommentCount());
        page.setPath("/discuss/detail/"+id);
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论VO列表
        ArrayList<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo = new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复的Vo列表
                ArrayList<Map<String,Object>> replyVoList = new ArrayList<>();
                if( replyList!= null){
                    for (Comment reply:replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }

                }

                commentVo.put("replies",replyVoList);
                //回复数量
                int count = commentService.findCountByEntity(ENTITY_TYPE_POST,comment.getId());
                commentVo.put("replyCount",count);
                commentVoList.add(commentVo);
            }

        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }
}
