package com.campus.bookborrow.mapper;

import com.campus.bookborrow.dto.BookDetailVO;
import com.campus.bookborrow.entity.BookInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图书信息 Mapper 接口
 * 所有多表联查 SQL 必须手写于对应的 XML 文件中
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Mapper
public interface BookInfoMapper {

    // ========== 库存操作（复用已有 XML） ==========

    /** 原子性扣减库存（防超卖） */
    int updateCurrentStockDecrement(@Param("id") Long id);

    /** 归还恢复库存 */
    int updateCurrentStockIncrement(@Param("id") Long id);

    // ========== 图书查询 ==========

    /**
     * 条件分页查询图书列表（多表联查 book_info + book_category）
     */
    List<BookDetailVO> selectBookList(@Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("status") Integer status,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    /** 条件查询图书总数 */
    int countBookList(@Param("keyword") String keyword,
                      @Param("categoryId") Long categoryId,
                      @Param("status") Integer status);

    /** 根据ID查询图书详情（联查分类名） */
    BookDetailVO selectBookDetail(@Param("id") Long id);

    /** 根据ISBN查询 */
    BookInfo selectByIsbn(@Param("isbn") String isbn);

    /** 根据ID查询 */
    BookInfo selectById(@Param("id") Long id);

    // ========== 图书管理（管理员） ==========

    /** 新增图书 */
    int insertBook(BookInfo book);

    /** 更新图书信息 */
    int updateBook(BookInfo book);

    /** 下架/上架图书 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
