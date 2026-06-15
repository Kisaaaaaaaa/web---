package com.campus.bookborrow.service;

import com.campus.bookborrow.dto.BorrowRecordVO;

import java.util.List;
import java.util.Map;

/**
 * 借阅管理 Service 接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
public interface BorrowService {

    /**
     * 分页条件查询借阅记录（含动态逾期罚金计算）
     */
    Map<String, Object> listBorrowRecords(Long userId, String username,
                                           Integer status, String bookKeyword,
                                           int page, int pageSize);

    /** 借书操作 */
    void borrowBook(Long userId, Long bookId);

    /** 归还图书 */
    void returnBook(Long recordId);

    /** 续借图书 */
    void renewBook(Long recordId);

    /** 管理员手动标记逾期 */
    void markOverdue(Long recordId);

    /** 获取用户借阅统计 */
    Map<String, Object> getUserBorrowStats(Long userId);

    /** 获取借阅排行榜 */
    List<com.campus.bookborrow.dto.TopBookVO> getTopBooks(int limit);

    /** 获取用户逾期数量 */
    int getUserOverdueCount(Long userId);
}
