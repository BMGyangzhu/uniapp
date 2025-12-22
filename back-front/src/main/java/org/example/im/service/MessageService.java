package org.example.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.im.entity.Message;
import org.example.im.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author bgmyangzhu
 * @date 2025/12/2 18:17
 */
@Service
public class MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    public Message saveMessage(Long fromUserId, Long toUserId, String content) {
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setStatus(0); //默认未读
        messageMapper.insert(message);
        return message;
    }
    
    public Message getLastMessage(Long fromUserId, Long toUserId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getFromUserId,fromUserId)
                .eq(Message::getToUserId,toUserId)
                .or()
                .eq(Message::getFromUserId,toUserId)
                .eq(Message::getToUserId,fromUserId)
                .orderByDesc(Message::getGmtCreated)
                .last("LIMIT 1");
        
        return messageMapper.selectOne(queryWrapper);
    }
    
    public List<Message> getUnreadMessages(Long userId) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId,userId)
                .eq(Message::getStatus,0)
                .orderByAsc(Message::getGmtCreated));
    }
    
    public void markRead(Long messageId) {
        Message message = new Message();
        message.setId(messageId);
        message.setStatus(1);
        messageMapper.updateById(message);
    }
    
    public List<Message> getHistory(Long user1, Long user2) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getFromUserId,user1)
                        .eq(Message::getToUserId,user2)
                        .or()
                        .eq(Message::getFromUserId,user2)
                        .eq(Message::getToUserId,user1)
                        .orderByAsc(Message::getGmtCreated)
        );
    }
    public Map<Long, Long> countUnreadByUser(Long userId, Set<Long> toUserIds) {
        List<Message> list = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getStatus, 0)
                        .in(!toUserIds.isEmpty(), Message::getFromUserId, toUserIds)
        );

        return list.stream()
                   .collect(Collectors.groupingBy(Message::getFromUserId, Collectors.counting()));
    }

    public Integer countUnread(Long userId, Long toUserId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getFromUserId, toUserId)
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getStatus, 0)
        );
    }

    public void markMessagesAsRead(Long fromUserId, Long toUserId) {
        // 查询未读消息
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getFromUserId, fromUserId)
                    .eq(Message::getToUserId, toUserId)
                    .eq(Message::getStatus, 0);

        List<Message> unreadList = messageMapper.selectList(queryWrapper);
        if (unreadList != null && !unreadList.isEmpty()) {
            for (Message msg : unreadList) {
                msg.setStatus(1); // 标记为已读
                messageMapper.updateById(msg);
            }
        }
    }
}
