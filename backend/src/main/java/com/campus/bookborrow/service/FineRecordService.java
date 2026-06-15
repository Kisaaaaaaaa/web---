package com.campus.bookborrow.service;

import com.campus.bookborrow.entity.FineRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 罚金管理 Service 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-15
 */
public interface FineRecordService {

    /** 分页条件查询罚金记录 */
    Map<String, Object> listFineRecords(Long userId, String username,
                                         Integer paymentStatus, int page, int pageSize);

    /** 创建罚金记录（标记逾期时调用） */
    void createFineRecord(Long borrowId, Long userId, Long bookId,
                           int overdueDays, BigDecimal fineAmount);

    /** 更新罚金金额（归还时重新计算） */
    void updateFineOnReturn(Long borrowId, int overdueDays, BigDecimal fineAmount);

    /** 更新缴费状态 */
    void updatePaymentStatus(Long id, Integer paymentStatus);

    /** 获取用户未缴罚金总额 */
    BigDecimal getUnpaidFineTotal(Long userId);

    /** 罚金状态分布统计 */
    List<Map<String, Object>> getFineStatusDistribution();

    /** 根据ID查询罚金记录 */
    FineRecord getById(Long id);
}
