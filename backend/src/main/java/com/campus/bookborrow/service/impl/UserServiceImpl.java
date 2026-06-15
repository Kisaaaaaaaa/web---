package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.entity.SysUser;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.UserMapper;
import com.campus.bookborrow.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public Map<String, Object> listUsers(String username, String realName,
                                          Long roleId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<SysUser> list = userMapper.selectUserList(username, realName, roleId, offset, pageSize);
        int total = userMapper.countUserList(username, realName, roleId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list); result.put("total", total);
        result.put("page", page); result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public SysUser getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        return user;
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (userMapper.selectById(id) == null) throw new BusinessException(404, "用户不存在");
        if (status != 0 && status != 1) throw new BusinessException(400, "状态值非法");
        userMapper.updateStatus(id, status);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        if (userMapper.selectById(user.getId()) == null) throw new BusinessException(404, "用户不存在");
        userMapper.updateUser(user);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        String oldEnc = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!oldEnc.equals(user.getPassword())) throw new BusinessException(400, "原密码错误");
        String newEnc = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));
        userMapper.updatePassword(userId, newEnc);
        log.info("密码已修改 → userId={}", userId);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void updateProfile(SysUser user) {
        if (userMapper.selectById(user.getId()) == null) throw new BusinessException(404, "用户不存在");
        if (user.getStudentNo() != null && !user.getStudentNo().isEmpty()
                && userMapper.countByStudentNoExcludeSelf(user.getStudentNo(), user.getId()) > 0) {
            throw new BusinessException(400, "该学号已被其他用户使用");
        }
        userMapper.updateProfile(user);
        log.info("个人信息已更新 → userId={}", user.getId());
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) throw new BusinessException(400, "头像URL不能为空");
        userMapper.updateAvatar(userId, avatarUrl);
        log.info("头像已更新 → userId={}", userId);
    }
}
