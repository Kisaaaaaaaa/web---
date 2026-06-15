package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.BookCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图书分类 Mapper 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Mapper
public interface BookCategoryMapper {

    /** 查询所有分类（含层级结构） */
    List<BookCategory> selectAll();

    /** 根据父ID查询子分类 */
    List<BookCategory> selectByParentId(@Param("parentId") Long parentId);

    /** 根据ID查询 */
    BookCategory selectById(@Param("id") Long id);

    /** 插入分类 */
    int insertCategory(BookCategory category);

    /** 更新分类 */
    int updateCategory(BookCategory category);

    /** 删除分类（逻辑删除） */
    int deleteById(@Param("id") Long id);
}
