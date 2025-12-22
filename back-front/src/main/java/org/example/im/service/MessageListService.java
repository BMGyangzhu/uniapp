package org.example.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.im.entity.MessageList;
import org.example.im.entity.User;
import org.example.im.mapper.MessageListMapper;
import org.example.im.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageListService {

    @Autowired
    private MessageListMapper messageListMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    /**
     * 获取联系人列表，并填充昵称、头像和未读数
     */
    public List<MessageList> getMessageLists(Long userId) {

        // 查询联系人列表
        LambdaQueryWrapper<MessageList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageList::getUserId, userId);
        List<MessageList> messageLists = messageListMapper.selectList(queryWrapper);

        if (messageLists.isEmpty()) {
            return messageLists;
        }

        // 获取所有联系人ID
        Set<Long> toUserIds = messageLists.stream()
                                          .map(MessageList::getToUserId)
                                          .collect(Collectors.toSet());

        // 获取联系人基本信息
        Map<Long, User> userInfoMap = userService.getUsersBaseInfoMap(toUserIds);

        // 批量统计未读数
        Map<Long, Long> unreadMap = messageService.countUnreadByUser(userId, toUserIds);

        // 填充信息
        messageLists.forEach(item -> {
            User user = userInfoMap.get(item.getToUserId());
            if (user != null) {
                item.setToUserAvatar(user.getAvatar());
                item.setNickName(user.getNickName());
            }

            // 填充未读数
            item.setUnreadCount(unreadMap.getOrDefault(item.getToUserId(), 0L));
        });

        return messageLists;
    }

    public MessageList getMessageList(MessageList messageList) {
        LambdaQueryWrapper<MessageList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageList::getUserId, messageList.getUserId())
                    .eq(MessageList::getToUserId, messageList.getToUserId());
        MessageList one = messageListMapper.selectOne(queryWrapper);

        if (one != null) {
            User single = userService.getSingle(one.getToUserId());
            one.setNickName(single.getNickName());
            one.setToUserAvatar(single.getAvatar());

        }

        return one;
    }

    
    public void addContact(MessageList messageList) {
        messageListMapper.insert(messageList);
    }

    public void updateLastMessageTime(Long fromUserId, Long toUserId, Date lastMessageTime) {

        QueryWrapper<MessageList> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("user_id", fromUserId)
                .eq("to_user_id", toUserId);

        MessageList update1 = new MessageList(fromUserId, toUserId);
        update1.setLastMessageTime(lastMessageTime);

        messageListMapper.update(update1, wrapper1);

        QueryWrapper<MessageList> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("user_id", toUserId)
                .eq("to_user_id", fromUserId);

        MessageList update2 = new MessageList(toUserId, fromUserId);
        update2.setLastMessageTime(lastMessageTime);

        messageListMapper.update(update2, wrapper2);
    }

}
