package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.Hostholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;


@Controller
public class MessageController {

    @Autowired
    private Hostholder hostholder;
    @Autowired
    private MessageService messageService;


    @Autowired
    private UserService userService;

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
                map.put("unreadCount",messageService.findUnreadLetter(user.getId(), conversationId));
                int targetId = user.getId() == message.getFromId()? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        int letterUnreadCount = messageService.findUnreadLetter(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }


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

}
