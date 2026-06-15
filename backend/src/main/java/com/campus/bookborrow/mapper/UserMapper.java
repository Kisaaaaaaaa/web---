package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper 接口
 * 所有 SQL 手写于对应 XML 文件中
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Mapper
public interface UserMapper {

    /** 根据用户名查询用户（登录用） */
    SysUser selectByUsername(@Param("username") String username);

    /** 根据ID查询用户 */
    SysUser selectById(@Param("id") Long id);

    /** 查询用户名是否已存在 */
    int countByUsername(@Param("username") String username);

    /** 查询学号是否已存在 */
    int countByStudentNo(@Param("studentNo") String studentNo);

    /** 统计除自己外同学号的用户数 */
    int countByStudentNoExcludeSelf(@Param("studentNo") String studentNo, @Param("userId") Long userId);

    /** 插入新用户（注册） */
    int insertUser(SysUser user);

    /** 分页条件查询用户列表（管理员） */
    List<SysUser> selectUserList(@Param("username") String username,
                                  @Param("realName") String realName,
                                  @Param("roleId") Long roleId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /** 条件查询用户总数 */
    int countUserList(@Param("username") String username,
                      @Param("realName") String realName,
                      @Param("roleId") Long roleId);

    /** 更新用户状态（启用/禁用） */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 更新用户信息 */
    int updateUser(SysUser user);

    /** 修改密码 */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 修改个人信息（不包含密码和角色） */
    int updateProfile(SysUser user);

    /** 更新头像 */
    int updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);
}
