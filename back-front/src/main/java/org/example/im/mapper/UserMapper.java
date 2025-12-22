package org.example.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.im.entity.User;

/**
 * @author bgmyangzhu
 * @date 2025/12/3 19:17
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
