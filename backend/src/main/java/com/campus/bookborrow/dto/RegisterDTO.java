package com.campus.bookborrow.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在3-50之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在6-100之间")
    private String password;
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    private String studentNo;
    private String phone;
    private String email;
    private Long roleId = 2L;
}
