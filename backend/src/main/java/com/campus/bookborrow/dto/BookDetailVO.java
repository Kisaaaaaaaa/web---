package com.campus.bookborrow.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书详情视图对象（多表联查：book_info + book_category）
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
public class BookDetailVO {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishDate;
    private String coverUrl;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Integer totalStock;
    private Integer currentStock;
    private Integer status;
    private LocalDateTime createTime;

    /** 库存百分比（前端用） */
    public Integer getStockPercentage() {
        if (totalStock == null || totalStock == 0) return 0;
        return (int) Math.round((double) currentStock / totalStock * 100);
    }
}
