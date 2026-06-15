package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.entity.SysUser;
import com.campus.bookborrow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器（管理员专用）
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 分页查询用户列表 */
    @GetMapping
    public Result<Map<String, Object>> listUsers(
            @RequestAttribute("roleCode") String roleCode,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        return Result.ok(userService.listUsers(username, realName, roleId, page, pageSize));
    }

    /** 查询用户详情 */
    @GetMapping("/{id}")
    public Result<?> getUserById(@RequestAttribute("roleCode") String roleCode,
                                  @PathVariable Long id) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        return Result.ok(userService.getUserById(id));
    }

    /** 当前登录用户查看自己的信息 */
    @GetMapping("/me")
    public Result<?> getMyInfo(@RequestAttribute("userId") Long userId) {
        return Result.ok(userService.getUserById(userId));
    }

    /** 更新用户状态（启用/禁用） */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@RequestAttribute("roleCode") String roleCode,
                                   @PathVariable Long id,
                                   @RequestParam Integer status) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        userService.updateStatus(id, status);
        return Result.ok(status == 1 ? "用户已启用" : "用户已禁用");
    }

    /** 更新用户信息 */
    @PutMapping("/{id}")
    public Result<?> updateUser(@RequestAttribute("roleCode") String roleCode,
                                 @PathVariable Long id,
                                 @RequestBody SysUser user) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        user.setId(id);
        userService.updateUser(user);
        return Result.ok("更新用户信息成功");
    }

    /** 当前用户修改自己的密码 */
    @PutMapping("/profile/password")
    public Result<?> updatePassword(@RequestAttribute("userId") Long userId,
                                     @RequestBody Map<String, String> body) {
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (oldPwd == null || newPwd == null || newPwd.length() < 6)
            return Result.error(400, "密码不能为空且不少于6位");
        userService.updatePassword(userId, oldPwd, newPwd);
        return Result.ok("密码修改成功");
    }

    /** 当前用户修改自己的信息 */
    @PutMapping("/profile/info")
    public Result<?> updateProfile(@RequestAttribute("userId") Long userId,
                                    @RequestBody SysUser user) {
        user.setId(userId);
        userService.updateProfile(user);
        return Result.ok("信息更新成功");
    }

    /** 更新头像 */
    @PutMapping("/profile/avatar")
    public Result<?> updateAvatar(@RequestAttribute("userId") Long userId,
                                   @RequestBody Map<String, String> body) {
        userService.updateAvatar(userId, body.get("avatarUrl"));
        return Result.ok("头像更新成功");
    }
}
