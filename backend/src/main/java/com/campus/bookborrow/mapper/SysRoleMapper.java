package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Mapper
public interface SysRoleMapper {

    SysRole selectById(@Param("id") Long id);

    List<SysRole> selectAll();
}
