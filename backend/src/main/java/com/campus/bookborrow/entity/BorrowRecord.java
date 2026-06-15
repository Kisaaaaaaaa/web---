package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 * 对应数据库表：borrow_record
 *
 * 状态机说明（status 字段）：
 *   0 - 借阅中（已借出未归还，且未超过应还日期）
 *   1 - 已续借（在借阅中状态下申请续借成功）
 *   2 - 已归还（图书已归还入库，终态）
 *   3 - 已逾期（超过应还日期仍未归还）
 *
 * 状态流转规则：
 *   借阅中(0) ──申请续借成功──▶ 已续借(1)
 *   借阅中(0) ──归还图书─────▶ 已归还(2)
 *   借阅中(0) ──超过应还日期──▶ 已逾期(3)
 *   已续借(1) ──归还图书─────▶ 已归还(2)
 *   已续借(1) ──超过应还日期──▶ 已逾期(3)
 *   已逾期(3) ──归还图书─────▶ 已归还(2)
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    /** 借阅记录ID，主键自增 */
    private Long id;

    /** 借阅用户ID，关联 sys_user.id */
    private Long userId;

    /** 所借图书ID，关联 book_info.id */
    private Long bookId;

    /** 借阅时间（实际借出时间） */
    private LocalDateTime borrowTime;

    /** 应还时间（借阅时间 + 借阅期限） */
    private LocalDateTime dueTime;

    /** 实际归还时间（为空表示尚未归还） */
    private LocalDateTime returnTime;

    /** 续借时间（最近一次续借操作的时间） */
    private LocalDateTime renewTime;

    /** 续借次数（默认最多续借1次） */
    private Integer renewCount;

    /**
     * 借阅状态（状态机核心字段）：
     * 0 - 借阅中
     * 1 - 已续借
     * 2 - 已归还
     * 3 - 已逾期
     */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer isDeleted;

    // ==================== 状态机常量 ====================

    /** 状态：借阅中 */
    public static final int STATUS_BORROWING = 0;
    /** 状态：已续借 */
    public static final int STATUS_RENEWED = 1;
    /** 状态：已归还 */
    public static final int STATUS_RETURNED = 2;
    /** 状态：已逾期 */
    public static final int STATUS_OVERDUE = 3;
}
