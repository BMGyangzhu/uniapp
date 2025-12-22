package org.example.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author bgmyangzhu
 * @date 2025/12/3 18:52
 */
@Data
public class MessageList {

    public MessageList(){};
    public MessageList(Long fromUserId, Long toUserId) {
        this.userId = fromUserId;
        this.toUserId = toUserId;
    }
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(exist = false)
    private String nickName;
    
    private Long userId;
    
    private Long toUserId;
    
    @TableField(exist = false)
    private Long toUserAvatar;

    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date gmtUpdated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lastMessageTime;
    
    private Long unreadCount;
    
}
