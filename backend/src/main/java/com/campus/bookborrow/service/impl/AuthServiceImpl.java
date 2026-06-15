package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.dto.LoginDTO;
import com.campus.bookborrow.dto.LoginVO;
import com.campus.bookborrow.dto.RegisterDTO;
import com.campus.bookborrow.entity.SysRole;
import com.campus.bookborrow.entity.SysUser;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.UserMapper;
import com.campus.bookborrow.mapper.SysRoleMapper;
import com.campus.bookborrow.service.AuthService;
import com.campus.bookborrow.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 认证服务实现
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 3. 校验密码（MD5 加盐方式，生产建议用 BCrypt）
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 4. 查询角色
        SysRole role = roleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "ROLE_STUDENT";

        // 5. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
                roleCode, user.getRealName());

        log.info("用户登录成功 → username={}, role={}", user.getUsername(), roleCode);

        return new LoginVO(token, user.getId(), user.getUsername(), user.getRealName(), roleCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 校验用户名唯一性
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 2. 学号唯一性校验（如果填写了学号）
        if (dto.getStudentNo() != null && !dto.getStudentNo().isEmpty()) {
            if (userMapper.countByStudentNo(dto.getStudentNo()) > 0) {
                throw new BusinessException(400, "该学号已被注册");
            }
        }

        // 3. 密码加密（MD5）
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                dto.getPassword().getBytes(StandardCharsets.UTF_8));

        // 4. 组装用户对象
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encryptedPassword);
        user.setRealName(dto.getRealName());
        user.setStudentNo(dto.getStudentNo());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRoleId(dto.getRoleId() != null ? dto.getRoleId() : 2L);
        user.setStatus(1); // 默认启用

        // 5. 插入数据库
        int rows = userMapper.insertUser(user);
        if (rows == 0) {
            throw new BusinessException(500, "注册失败，请稍后重试");
        }

        log.info("新用户注册成功 → username={}, roleId={}", dto.getUsername(), user.getRoleId());
    }
}
