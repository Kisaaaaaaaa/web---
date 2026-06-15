package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书分类实体类
 * 对应数据库表：book_category
 * 支持层级分类（通过 parentId 自关联实现父子分类）
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCategory {

    /** 分类ID，主键自增 */
    private Long id;

    /** 分类名称（如：计算机科学、文学小说） */
    private String categoryName;

    /** 父分类ID，0表示顶级分类 */
    private Long parentId;

    /** 排序序号（数值越小越靠前） */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer isDeleted;
}
