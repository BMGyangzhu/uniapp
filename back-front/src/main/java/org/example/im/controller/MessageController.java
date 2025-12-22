package org.example.im.controller;

import org.example.im.entity.Message;
import org.example.im.entity.MessageList;
import org.example.im.service.MessageListService;
import org.example.im.service.MessageService;
import org.example.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bgmyangzhu
 * @date 2025/12/2 19:46
 */
@RestController
@RequestMapping("/im")
@CrossOrigin(origins = "*")
public class MessageController {
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private MessageListService messageListService;

    @GetMapping("/history")
    public List<Message> history(@RequestParam Long user1, @RequestParam Long user2) {
        return messageService.getHistory(user1, user2);
    }

    @GetMapping("/unread")
    public List<Message> unread(@RequestParam Long userId) {
        return messageService.getUnreadMessages(userId);
    }

    // 标记单条消息为已读
    @PostMapping("/read")
    public void markRead(@RequestParam Long messageId) {
        messageService.markRead(messageId);
    }
    
    @GetMapping("/getMessageLists/{id}")
    public List<MessageList> getMessageLists(@PathVariable Long id) {
        return messageListService.getMessageLists(id);
    }
    
    @PostMapping("/getMessageList")
    public MessageList getMessageList(@RequestBody MessageList messageList) {
        return messageListService.getMessageList(messageList);
    }
    
    @PostMapping("/addContact")
    public void addContact(@RequestBody MessageList messageList) {
         messageListService.addContact(messageList);
    }

    @PostMapping("/readAll")
    public void markReadByUser(@RequestParam Long userId, @RequestParam Long toUserId) {
        messageService.markMessagesAsRead(userId, toUserId);
    }
}
