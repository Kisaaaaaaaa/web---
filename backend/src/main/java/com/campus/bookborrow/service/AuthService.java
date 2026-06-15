package com.campus.bookborrow.service;

import com.campus.bookborrow.dto.LoginDTO;
import com.campus.bookborrow.dto.LoginVO;
import com.campus.bookborrow.dto.RegisterDTO;

/**
 * 认证服务接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
public interface AuthService {

    /** 用户登录 */
    LoginVO login(LoginDTO dto);

    /** 用户注册 */
    void register(RegisterDTO dto);
}
