package com.campus.bookborrow.service;

import com.campus.bookborrow.entity.BookCategory;

import java.util.List;
import java.util.Map;

/**
 * 分类管理 Service 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
public interface CategoryService {

    /** 获取分类树（含父子层级） */
    List<Map<String, Object>> getCategoryTree();

    /** 查询所有分类（平铺） */
    List<BookCategory> listAll();

    /** 新增分类 */
    void addCategory(BookCategory category);

    /** 更新分类 */
    void updateCategory(BookCategory category);

    /** 删除分类 */
    void deleteCategory(Long id);
}
