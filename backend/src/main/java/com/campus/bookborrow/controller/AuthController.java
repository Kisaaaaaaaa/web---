package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.dto.LoginDTO;
import com.campus.bookborrow.dto.RegisterDTO;
import com.campus.bookborrow.service.AuthService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册请求
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 用户登录 */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /** 用户注册 */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.ok("注册成功");
    }
}
