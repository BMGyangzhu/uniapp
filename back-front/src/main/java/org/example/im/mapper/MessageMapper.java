package org.example.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.im.entity.Message;

/**
 * @author bgmyangzhu
 * @date 2025/12/2 18:19
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
}
