package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.dto.BorrowRecordVO;
import com.campus.bookborrow.dto.TopBookVO;
import com.campus.bookborrow.entity.BorrowRecord;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.BookInfoMapper;
import com.campus.bookborrow.mapper.BorrowRecordMapper;
import com.campus.bookborrow.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅管理 Service 实现类（核心业务层）
 *
 * 状态机流转：
 *   借阅中(0) ──续借──▶ 已续借(1)
 *   借阅中(0) ──归还──▶ 已归还(2)
 *   借阅中(0) ──超期──▶ 已逾期(3)
 *   已续借(1) ──归还──▶ 已归还(2)
 *   已续借(1) ──超期──▶ 已逾期(3)
 *   已逾期(3) ──归还──▶ 已归还(2)
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookInfoMapper bookInfoMapper;

    @Value("${borrow.duration-days:30}")
    private int borrowDurationDays;

    @Value("${borrow.max-renew-count:1}")
    private int maxRenewCount;

    @Value("${borrow.max-borrow-count:5}")
    private int maxBorrowCount;

    // ================================================================
    //  借阅记录查询
    // ================================================================

    @Override
    public Map<String, Object> listBorrowRecords(Long userId, String username,
                                                  Integer status, String bookKeyword,
                                                  int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<BorrowRecordVO> list = borrowRecordMapper.selectBorrowRecordsWithFine(
                userId, username, status, bookKeyword, offset, pageSize);
        int total = borrowRecordMapper.countBorrowRecords(userId, username, status, bookKeyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    // ================================================================
    //  借书操作（核心）
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrowBook(Long userId, Long bookId) {
        log.info("========== 借书开始 → userId={}, bookId={} ==========", userId, bookId);

        // 步骤1：检查用户最大同时借阅数量
        int activeCount = borrowRecordMapper.countUserActiveBorrows(userId);
        if (activeCount >= maxBorrowCount) {
            throw new BusinessException(4002, "您当前有" + activeCount + "本图书未归还，最多同时借阅" + maxBorrowCount + "本");
        }

        // 步骤2：校验是否重复借阅同一本书
        int dupCount = borrowRecordMapper.countActiveBorrow(userId, bookId);
        if (dupCount > 0) {
            throw new BusinessException(4002, "该图书您已借阅且尚未归还，请勿重复借阅");
        }
        log.info("步骤1-2 通过 → 无重复借阅且借阅数量未超限");

        // 步骤3：原子性扣减库存（行锁防超卖）
        int affectedRows = bookInfoMapper.updateCurrentStockDecrement(bookId);
        if (affectedRows == 0) {
            throw new BusinessException(4001, "图书库存不足，借阅失败");
        }
        log.info("步骤3 通过 → 库存扣减成功");

        // 步骤4：插入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowTime(LocalDateTime.now());
        record.setDueTime(LocalDateTime.now().plusDays(borrowDurationDays));
        record.setRenewCount(0);
        record.setStatus(BorrowRecord.STATUS_BORROWING);

        int insertRows = borrowRecordMapper.insertBorrowRecord(record);
        if (insertRows == 0) {
            throw new BusinessException(500, "借阅记录创建失败，事务已回滚");
        }

        log.info("========== 借书完成 → recordId={}, dueTime={} ==========",
                record.getId(), record.getDueTime());
    }

    // ================================================================
    //  归还操作
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnBook(Long recordId) {
        log.info("========== 归还开始 → recordId={} ==========", recordId);

        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }

        int currentStatus = record.getStatus();
        // 已归还不可重复归还
        if (currentStatus == BorrowRecord.STATUS_RETURNED) {
            throw new BusinessException(4003, "该图书已经归还，请勿重复操作");
        }
        // 状态合法性校验
        if (currentStatus != BorrowRecord.STATUS_BORROWING
                && currentStatus != BorrowRecord.STATUS_RENEWED
                && currentStatus != BorrowRecord.STATUS_OVERDUE) {
            throw new BusinessException(4003, "当前借阅状态异常，无法归还");
        }

        int updateRows = borrowRecordMapper.updateStatus(recordId, BorrowRecord.STATUS_RETURNED);
        if (updateRows == 0) {
            throw new BusinessException(500, "更新借阅状态失败");
        }

        int stockRows = bookInfoMapper.updateCurrentStockIncrement(record.getBookId());
        if (stockRows == 0) {
            throw new BusinessException(500, "恢复图书库存失败");
        }

        log.info("========== 归还完成 → recordId={}, 原status={} ==========", recordId, currentStatus);
    }

    // ================================================================
    //  续借操作
    //  状态流转：借阅中(0) → 已续借(1)
    //  限制条件：每本书最多续借1次，逾期不可续借
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renewBook(Long recordId) {
        log.info("========== 续借开始 → recordId={} ==========", recordId);

        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }

        // 只有"借阅中"或"已续借"状态可以续借
        if (record.getStatus() != BorrowRecord.STATUS_BORROWING
                && record.getStatus() != BorrowRecord.STATUS_RENEWED) {
            throw new BusinessException(4003,
                    record.getStatus() == 2 ? "该图书已归还，无法续借" :
                    record.getStatus() == 3 ? "该图书已逾期，请先归还再借" : "状态异常，无法续借");
        }

        // 续借次数限制
        if (record.getRenewCount() >= maxRenewCount) {
            throw new BusinessException(4003, "续借次数已达上限（" + maxRenewCount + "次）");
        }

        int updateRows = borrowRecordMapper.updateRenew(recordId, maxRenewCount);
        if (updateRows == 0) {
            throw new BusinessException(500, "续借失败（可能已达续借次数上限）");
        }

        log.info("========== 续借完成 → recordId={}, 新dueTime=原+{}天 ==========", recordId, borrowDurationDays);
    }

    // ================================================================
    //  管理员手动标记逾期
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOverdue(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        if (record.getStatus() != BorrowRecord.STATUS_BORROWING
                && record.getStatus() != BorrowRecord.STATUS_RENEWED) {
            throw new BusinessException(4003, "当前状态无法标记为逾期");
        }
        int rows = borrowRecordMapper.updateStatus(recordId, BorrowRecord.STATUS_OVERDUE);
        if (rows == 0) {
            throw new BusinessException(500, "标记逾期失败");
        }
        log.info("管理员手动标记逾期 → recordId={}", recordId);
    }

    // ================================================================
    //  用户借阅统计（仪表板用）
    // ================================================================

    @Override
    public Map<String, Object> getUserBorrowStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 按状态统计
        List<Map<String, Object>> statusCounts = borrowRecordMapper.countByStatus(userId);
        long borrowing = 0, renewed = 0, returned = 0, overdue = 0;
        for (Map<String, Object> row : statusCounts) {
            // MySQL JDBC 会将 TINYINT(1) 映射为 Boolean，通过 toString() 再 parseInt 兼容两种类型
            int s = Integer.parseInt(String.valueOf(row.get("status")));
            long c = Long.parseLong(String.valueOf(row.get("cnt")));
            if (s == 0) borrowing = c;
            else if (s == 1) renewed = c;
            else if (s == 2) returned = c;
            else if (s == 3) overdue = c;
        }

        stats.put("borrowing", borrowing);
        stats.put("renewed", renewed);
        stats.put("returned", returned);
        stats.put("overdue", overdue);
        stats.put("total", borrowing + renewed + returned + overdue);

        // 活跃借阅数
        if (userId != null) {
            stats.put("activeBorrows", borrowRecordMapper.countUserActiveBorrows(userId));
        }

        return stats;
    }

    @Override
    public List<TopBookVO> getTopBooks(int limit) {
        return borrowRecordMapper.selectTopBooks(limit);
    }

    @Override
    public int getUserOverdueCount(Long userId) {
        if (userId != null) {
            return borrowRecordMapper.countUserOverdue(userId);
        }
        return borrowRecordMapper.countAllOverdue();
    }

    @Override
    public List<Map<String, Object>> getBorrowCountByCategory() {
        return borrowRecordMapper.selectBorrowCountByCategory();
    }

    @Override
    public List<Map<String, Object>> getDailyBorrowStats() {
        return borrowRecordMapper.selectDailyBorrowStats();
    }

    @Override
    public List<Map<String, Object>> getStatusDistribution() {
        return borrowRecordMapper.selectStatusDistribution();
    }
}
