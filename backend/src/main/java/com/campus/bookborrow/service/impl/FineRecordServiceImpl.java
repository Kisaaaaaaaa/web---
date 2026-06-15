package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.entity.FineRecord;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.FineRecordMapper;
import com.campus.bookborrow.service.FineRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 罚金管理 Service 实现类
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FineRecordServiceImpl implements FineRecordService {

    private final FineRecordMapper fineRecordMapper;

    @Override
    public Map<String, Object> listFineRecords(Long userId, String username,
                                                Integer paymentStatus, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<FineRecord> list = fineRecordMapper.selectFineRecords(
                userId, username, paymentStatus, offset, pageSize);
        int total = fineRecordMapper.countFineRecords(userId, username, paymentStatus);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFineRecord(Long borrowId, Long userId, Long bookId,
                                  int overdueDays, BigDecimal fineAmount) {
        // 检查是否已存在罚金记录
        FineRecord exist = fineRecordMapper.selectByBorrowId(borrowId);
        if (exist != null) {
            // 已存在则更新
            fineRecordMapper.updateFineAmount(exist.getId(), overdueDays, fineAmount);
            log.info("罚金记录已更新 → borrowId={}, overdueDays={}, fineAmount={}",
                    borrowId, overdueDays, fineAmount);
            return;
        }

        FineRecord record = new FineRecord();
        record.setBorrowId(borrowId);
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setOverdueDays(overdueDays);
        record.setFineAmount(fineAmount);
        record.setPaymentStatus(FineRecord.PAY_UNPAID);

        int rows = fineRecordMapper.insertFineRecord(record);
        if (rows == 0) {
            throw new BusinessException(500, "罚金记录创建失败");
        }
        log.info("罚金记录已生成 → borrowId={}, overdueDays={}, fineAmount={}",
                borrowId, overdueDays, fineAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFineOnReturn(Long borrowId, int overdueDays, BigDecimal fineAmount) {
        FineRecord exist = fineRecordMapper.selectByBorrowId(borrowId);
        if (exist != null) {
            fineRecordMapper.updateFineAmount(exist.getId(), overdueDays, fineAmount);
            log.info("归还时罚金已更新 → borrowId={}, overdueDays={}, fineAmount={}",
                    borrowId, overdueDays, fineAmount);
        } else if (overdueDays > 0) {
            // 如果之前没有罚金记录（例如系统自动检测到逾期），则创建
            log.info("归还时未找到罚金记录，跳过 → borrowId={}", borrowId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(Long id, Integer paymentStatus) {
        FineRecord exist = fineRecordMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "罚金记录不存在");
        }
        int rows = fineRecordMapper.updatePaymentStatus(id, paymentStatus);
        if (rows == 0) {
            throw new BusinessException(500, "更新缴费状态失败");
        }
        log.info("缴费状态已更新 → id={}, paymentStatus={}", id, paymentStatus);
    }

    @Override
    public BigDecimal getUnpaidFineTotal(Long userId) {
        if (userId != null) {
            return fineRecordMapper.sumUnpaidFine(userId);
        }
        return fineRecordMapper.sumAllUnpaidFine();
    }

    @Override
    public List<Map<String, Object>> getFineStatusDistribution() {
        return fineRecordMapper.selectFineStatusDistribution();
    }

    @Override
    public FineRecord getById(Long id) {
        FineRecord record = fineRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "罚金记录不存在");
        }
        return record;
    }
}
