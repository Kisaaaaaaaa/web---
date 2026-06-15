package com.campus.bookborrow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 VO
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
@AllArgsConstructor
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色编码 */
    private String roleCode;
}
