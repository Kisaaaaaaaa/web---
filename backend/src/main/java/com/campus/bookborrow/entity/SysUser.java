package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 对应数据库表：sys_user
 * 包含管理员和学生两种角色
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {

    /** 用户ID，主键自增 */
    private Long id;

    /** 用户名（登录账号），唯一 */
    private String username;

    /** 登录密码（BCrypt加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 学号（学生角色必填，管理员可为空），唯一 */
    private String studentNo;

    /** 手机号码 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 头像URL */
    private String avatarUrl;

    /** 角色ID，关联 sys_role.id */
    private Long roleId;

    /** 账号状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer isDeleted;
}
