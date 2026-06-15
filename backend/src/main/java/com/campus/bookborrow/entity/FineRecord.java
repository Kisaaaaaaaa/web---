package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 罚金记录实体类
 * 对应数据库表：fine_record
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FineRecord {

    /** 罚金记录ID，主键自增 */
    private Long id;

    /** 关联借阅记录ID */
    private Long borrowId;

    /** 用户ID */
    private Long userId;

    /** 图书ID */
    private Long bookId;

    /** 逾期天数 */
    private Integer overdueDays;

    /** 罚金金额（元） */
    private BigDecimal fineAmount;

    /** 缴费状态：0-未缴，1-已缴 */
    private Integer paymentStatus;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer isDeleted;

    // ==================== 缴费状态常量 ====================

    /** 未缴 */
    public static final int PAY_UNPAID = 0;
    /** 已缴 */
    public static final int PAY_PAID = 1;

    // ==================== 联查字段（非表字段） ====================

    /** 用户名（联查 sys_user） */
    private String username;

    /** 真实姓名（联查 sys_user） */
    private String realName;

    /** 学号（联查 sys_user） */
    private String studentNo;

    /** 图书书名（联查 book_info） */
    private String bookTitle;

    /** 图书ISBN（联查 book_info） */
    private String isbn;
}
