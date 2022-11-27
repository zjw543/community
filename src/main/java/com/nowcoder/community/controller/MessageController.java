package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;


@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private Hostholder hostholder;
    @Autowired
    private MessageService messageService;


    @Autowired
    private UserService userService;

    //获得未读私信的id列表
    private List<Integer> getUnreadLetterIds(List<Message> messageList){
        List<Integer> ids = new ArrayList<>();
        if (messageList!=null){
            for (Message message:messageList){
                if(message.getToId()==hostholder.getUser().getId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    //私信列表
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Page page, Model model){
        User user = hostholder.getUser();
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");
        page.setLimit(5);
        //获取私信列表
        List<Message> conversationList = messageService.findConversation(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations= new ArrayList<>();
        if(conversationList!=null){
            for (Message message:conversationList){
                HashMap<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                String conversationId = message.getConversationId();
                map.put("letterCount",messageService.findLetersCount(conversationId));
                map.put("unreadCount",messageService.findUnreadLetterCount(user.getId(), conversationId));
                int targetId = user.getId() == message.getFromId()? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //查询未读通知数量
        model.addAttribute("unreadNoticeCount",messageService.findUnreadNoticeCount(user.getId(),null));

        return "/site/letter";
    }


    //私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId")String conversationId,Page page,Model model){
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetersCount(conversationId));
        page.setLimit(5);

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList!=null){
            for (Message message:letterList){
                HashMap<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getTargetUser(conversationId));

        //设置已读
        List<Integer> ids = getUnreadLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    //获取私信时的目标用户
    public User getTargetUser(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        User user0 = userService.findUserById(id0);
        User user1 = userService.findUserById(id1);

        //不能用==
        if (user0.equals(hostholder.getUser())){
            return user1;
        }else {
            return user0;
        }

    }

    //发送消息
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    public String sendMessage(String toName,String content){

        User targetUser = userService.findUserByName(toName);
        if(targetUser==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostholder.getUser().getId());
        message.setToId(targetUser.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        String ConversationId;
        int fromId = message.getFromId();
        int toId = message.getToId();
        if (fromId>toId){
            ConversationId = toId+"_"+ fromId;
        }else {
            ConversationId = fromId + "_" + toId;
        }
        message.setConversationId(ConversationId);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);

    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getLatestNotice(Model model){
        int userId = hostholder.getUser().getId();

        //评论类通知
        Message message = messageService.findLatestNotice(userId,TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message",message);
            String content =  HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVo.put("user",userService.findUserById((int)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            messageVo.put("noticeCount",messageService.findNoticeCount(userId,TOPIC_COMMENT));
            messageVo.put("unreadNoticeCount",messageService.findUnreadNoticeCount(userId,TOPIC_COMMENT));
            model.addAttribute("commentNotice",messageVo);
        }

        //点赞类通知
        message = messageService.findLatestNotice(userId,TOPIC_LIKE);
        messageVo = new HashMap<>();
        if(message!=null) {
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            messageVo.put("noticeCount", messageService.findNoticeCount(userId, TOPIC_LIKE));
            messageVo.put("unreadNoticeCount", messageService.findUnreadNoticeCount(userId, TOPIC_LIKE));
            model.addAttribute("likeNotice", messageVo);
        }



        //关注类通知
        message = messageService.findLatestNotice(userId,TOPIC_COMMENT);
        messageVo = new HashMap<>();
        if(message!=null) {
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            messageVo.put("noticeCount", messageService.findNoticeCount(userId, TOPIC_COMMENT));
            messageVo.put("unreadNoticeCount", messageService.findUnreadNoticeCount(userId, TOPIC_COMMENT));
            model.addAttribute("followNotice", messageVo);
        }

        //查询未读消息数量
        model.addAttribute("unreadLetterCount",messageService.findUnreadLetterCount(userId,null));
        //查询未读通知数量
        model.addAttribute("unreadNoticeCount",messageService.findUnreadNoticeCount(userId,null));
        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Model model,Page page){
        User user = hostholder.getUser();
        page.setPath("/notice/detail/"+topic);
        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        for(Message notice: notices){
            Map<String,Object> map = new HashMap<>();
            map.put("notice",notice);
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            map.put("user",userService.findUserById((Integer)data.get("userId")));
            map.put("entityType",data.get("entityType"));
            map.put("entityId",data.get("entityId"));
            map.put("postId",data.get("postId"));
            //通知作者
            map.put("fromUser",userService.findUserById(notice.getFromId()));
            noticeVoList.add(map);
        }
        model.addAttribute("notices",noticeVoList);
        List<Integer> ids = getUnreadLetterIds(notices);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }

}
