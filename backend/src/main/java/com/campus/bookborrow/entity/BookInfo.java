package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书信息实体类
 * 对应数据库表：book_info
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookInfo {

    /** 图书ID，主键自增 */
    private Long id;

    /** 国际标准书号（ISBN），唯一标识一本图书 */
    private String isbn;

    /** 图书标题/书名 */
    private String title;

    /** 作者 */
    private String author;

    /** 出版社 */
    private String publisher;

    /** 出版日期 */
    private LocalDate publishDate;

    /** 封面图片URL地址 */
    private String coverUrl;

    /** 分类ID，关联 book_category.id */
    private Long categoryId;

    /** 图书简介/内容摘要 */
    private String description;

    /** 总库存（馆藏数量） */
    private Integer totalStock;

    /** 当前可借数量 */
    private Integer currentStock;

    /** 图书状态：0-已下架，1-在架可借 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer isDeleted;
}
