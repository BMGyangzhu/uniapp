package org.example.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.im.entity.User;
import org.example.im.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bgmyangzhu
 * @date 2025/12/3 19:19
 */
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;

    public Map<Long, User> getUsersBaseInfoMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, ids)
                        .select(User::getId, User::getAvatar, User::getNickName)
        );

        return users.stream().collect(Collectors.toMap(
                User::getId,
                user -> user
        ));
    }
    
    public User getSingle(Long id) {
        return userMapper.selectById(id);
    }

    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
