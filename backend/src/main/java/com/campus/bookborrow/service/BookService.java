package com.campus.bookborrow.service;

import com.campus.bookborrow.dto.BookDetailVO;
import com.campus.bookborrow.entity.BookInfo;

import java.util.Map;

/**
 * 图书管理 Service 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
public interface BookService {

    /** 条件分页查询图书列表 */
    Map<String, Object> listBooks(String keyword, Long categoryId, Integer status,
                                   int page, int pageSize);

    /** 查询图书详情 */
    BookDetailVO getBookDetail(Long id);

    /** 新增图书（管理员） */
    void addBook(BookInfo book);

    /** 更新图书信息（管理员） */
    void updateBook(BookInfo book);

    /** 上架/下架图书（管理员） */
    void updateBookStatus(Long id, Integer status);
}
