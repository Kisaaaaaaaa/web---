package com.campus.bookborrow.service;

import com.campus.bookborrow.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 用户管理 Service 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
public interface UserService {

    /** 分页条件查询用户列表 */
    Map<String, Object> listUsers(String username, String realName, Long roleId,
                                   int page, int pageSize);

    /** 根据ID查询用户 */
    SysUser getUserById(Long id);

    /** 更新用户状态 */
    void updateStatus(Long id, Integer status);

    /** 更新用户信息（管理员） */
    void updateUser(SysUser user);
    /** 修改密码 */
    void updatePassword(Long userId, String oldPassword, String newPassword);
    /** 修改个人信息 */
    void updateProfile(SysUser user);
    /** 更新头像 */
    void updateAvatar(Long userId, String avatarUrl);
}
