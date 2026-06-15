package com.campus.bookborrow.mapper;

import com.campus.bookborrow.dto.BorrowRecordVO;
import com.campus.bookborrow.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 借阅记录 Mapper 接口
 * 所有多表联查 SQL 必须手写于对应的 XML 文件中
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Mapper
public interface BorrowRecordMapper {

    /**
     * 【核心查询】多表联查借阅记录，并动态计算逾期天数和罚金
     * 联查表：borrow_record + sys_user + book_info
     * 支持按用户过滤、用户名模糊查询、状态精确筛选、分页
     */
    List<BorrowRecordVO> selectBorrowRecordsWithFine(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("status") Integer status,
            @Param("bookKeyword") String bookKeyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /** 条件查询借阅记录总数 */
    int countBorrowRecords(@Param("userId") Long userId,
                           @Param("username") String username,
                           @Param("status") Integer status,
                           @Param("bookKeyword") String bookKeyword);

    /** 插入一条借阅记录 */
    int insertBorrowRecord(BorrowRecord record);

    /** 查询用户对某本书是否存在未归还的活跃借阅记录 */
    int countActiveBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /** 根据ID查询借阅记录 */
    BorrowRecord selectById(@Param("id") Long id);

    /** 更新借阅记录状态（归还/续借/逾期） */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 续借：更新 due_time + 状态 + renew_count */
    int updateRenew(@Param("id") Long id);

    /** 统计用户各状态借阅数量 */
    List<Map<String, Object>> countByStatus(@Param("userId") Long userId);

    /** 统计用户活跃借阅数量 */
    int countUserActiveBorrows(@Param("userId") Long userId);

    /** 借阅排行榜：按借阅次数排序 */
    List<com.campus.bookborrow.dto.TopBookVO> selectTopBooks(@Param("limit") int limit);

    /** 查询用户逾期数量 */
    int countUserOverdue(@Param("userId") Long userId);

    /** 查询所有逾期未还的记录数 */
    int countAllOverdue();

    /** 查询所有已逾期但状态未更新的记录 */
    List<BorrowRecord> selectOverdueRecords();
}
