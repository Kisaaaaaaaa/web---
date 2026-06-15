package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.FineRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 罚金记录 Mapper 接口
 * 所有 SQL 必须手写于对应的 XML 文件中
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-15
 */
@Mapper
public interface FineRecordMapper {

    /** 多表联查罚金记录（含用户、图书信息） */
    List<FineRecord> selectFineRecords(@Param("userId") Long userId,
                                        @Param("username") String username,
                                        @Param("paymentStatus") Integer paymentStatus,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    /** 条件查询罚金记录总数 */
    int countFineRecords(@Param("userId") Long userId,
                         @Param("username") String username,
                         @Param("paymentStatus") Integer paymentStatus);

    /** 插入罚金记录 */
    int insertFineRecord(FineRecord record);

    /** 根据ID查询 */
    FineRecord selectById(@Param("id") Long id);

    /** 根据借阅记录ID查询 */
    FineRecord selectByBorrowId(@Param("borrowId") Long borrowId);

    /** 更新缴费状态 */
    int updatePaymentStatus(@Param("id") Long id, @Param("paymentStatus") Integer paymentStatus);

    /** 更新罚金金额（归还时重新计算） */
    int updateFineAmount(@Param("id") Long id,
                          @Param("overdueDays") Integer overdueDays,
                          @Param("fineAmount") java.math.BigDecimal fineAmount);

    /** 统计用户未缴罚金总额 */
    java.math.BigDecimal sumUnpaidFine(@Param("userId") Long userId);

    /** 统计所有未缴罚金总额 */
    java.math.BigDecimal sumAllUnpaidFine();

    /** 罚金状态分布统计 */
    List<Map<String, Object>> selectFineStatusDistribution();
}
