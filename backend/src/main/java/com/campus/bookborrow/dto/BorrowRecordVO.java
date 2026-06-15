package com.campus.bookborrow.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录视图对象（VO）
 * 用于多表联查结果的封装，包含借阅信息、用户信息、图书信息及动态计算字段
 *
 * 对应 MyBatis XML 中 borrowRecordWithFine 查询的结果映射
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
public class BorrowRecordVO {

    // ==================== 借阅记录字段 ====================

    /** 借阅记录ID */
    private Long id;

    /** 借阅用户ID */
    private Long userId;

    /** 所借图书ID */
    private Long bookId;

    /** 借阅时间 */
    private LocalDateTime borrowTime;

    /** 应还时间 */
    private LocalDateTime dueTime;

    /** 实际归还时间 */
    private LocalDateTime returnTime;

    /** 续借时间 */
    private LocalDateTime renewTime;

    /** 续借次数 */
    private Integer renewCount;

    /** 借阅状态：0-借阅中，1-已续借，2-已归还，3-已逾期 */
    private Integer status;

    // ==================== 用户信息字段（关联 sys_user） ====================

    /** 借阅人用户名 */
    private String username;

    /** 借阅人真实姓名 */
    private String realName;

    /** 借阅人学号 */
    private String studentNo;

    // ==================== 图书信息字段（关联 book_info） ====================

    /** 图书ISBN */
    private String isbn;

    /** 图书书名 */
    private String bookTitle;

    /** 图书作者 */
    private String bookAuthor;

    /** 图书封面URL */
    private String coverUrl;

    // ==================== SQL 动态计算字段 ====================

    /**
     * 逾期天数
     * 计算规则：
     *   - 已归还(status=2)：return_time - due_time（若提前归还则为0或负数）
     *   - 未归还且已逾期：NOW() - due_time
     *   - 未逾期：0
     */
    private Integer overdueDays;

    /**
     * 动态罚金（单位：元）
     * 计算规则：逾期天数 × 0.5 元/天
     * 已归还或未逾期的记录罚金为 0
     */
    private BigDecimal dynamicFine;
}
