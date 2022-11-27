package com.nowcoder.community.service;

import com.nowcoder.community.dao.Messagemapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.filter.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private Messagemapper messagemapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversation(int userId,int offset,int limit){
        return messagemapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId){
        return messagemapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messagemapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetersCount(String conversationId){
        return messagemapper.selectLetterCount(conversationId);
    }

    public int findUnreadLetterCount(int userId, String conversationId){
        return messagemapper.selectLetterUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message){
        if(message!=null){
            message.setContent(HtmlUtils.htmlEscape(message.getContent()));
            message.setContent(sensitiveFilter.filter(message.getContent()));
            return messagemapper.insertMessage(message);
        }
        throw new RuntimeException("message cant be null");
    }

    public int readMessage(List<Integer> ids){
        return messagemapper.updateMessageStatus(ids,1);
    }

    public Message findLatestNotice(int userId,String topic){
        return messagemapper.selectLatestNotice(userId,topic);
    }

    public int findNoticeCount(int userId ,String topic){
        return messagemapper.selectNoticeCount(userId,topic);
    }

    public int findUnreadNoticeCount(int userId,String topic){
        return messagemapper.selectNoticeUnreadCount(userId,topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return messagemapper.selectNotices(userId,topic,offset,limit);
    }
}
