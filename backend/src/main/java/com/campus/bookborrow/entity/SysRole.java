package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统角色实体类
 * 对应数据库表：sys_role
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysRole {

    /** 角色ID，主键自增 */
    private Long id;

    /** 角色名称（如：系统管理员、学生） */
    private String roleName;

    /** 角色编码（如：ROLE_ADMIN, ROLE_STUDENT） */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer isDeleted;
}
