-- ============================================================
-- 罚金数据填充脚本
-- 从现有 borrow_record 中提取逾期记录生成罚金
-- 执行前确保 fine_record 表已创建
-- ============================================================

USE campus_book_borrow;

-- ============================================================
-- 1. 已逾期(status=3) 或 已归还但逾期归还的 → 生成罚金
--    overdue_days：已归还用 return_time-due_time，未归还用 NOW()-due_time
--    fine_amount：overdue_days × 0.5 元
--    payment_status：已归还的标记为未缴(0)，方便管理员查看历史罚金
-- ============================================================
INSERT INTO fine_record (borrow_id, user_id, book_id, overdue_days, fine_amount, payment_status)
SELECT
    br.id AS borrow_id,
    br.user_id,
    br.book_id,
    CASE
        -- 状态=2 已归还：逾期天数 = 归还时间 - 应还时间
        WHEN br.status = 2 THEN DATEDIFF(br.return_time, br.due_time)
        -- 状态=3 已逾期 或 状态=0/1但已过应还时间：逾期天数 = NOW() - 应还时间
        ELSE DATEDIFF(NOW(), br.due_time)
    END AS overdue_days,
    CASE
        WHEN br.status = 2 THEN DATEDIFF(br.return_time, br.due_time) * 0.5
        ELSE DATEDIFF(NOW(), br.due_time) * 0.5
    END AS fine_amount,
    0 AS payment_status  -- 默认未缴
FROM borrow_record br
WHERE br.is_deleted = 0
  -- 条件：已逾期(status=3) 或 已归还但归还晚于应还时间 或 借阅中/续借中但已过应还时间
  AND (
      br.status = 3  -- 已标记逾期
      OR (br.status = 2 AND br.return_time > br.due_time)  -- 逾期归还
      OR (br.status IN (0, 1) AND br.due_time < NOW())  -- 借阅中/续借中但已过期
  )
  -- 避免重复插入（同一借阅记录只生成一条罚金）
  AND NOT EXISTS (
      SELECT 1 FROM fine_record fr
      WHERE fr.borrow_id = br.id AND fr.is_deleted = 0
  )
  -- 逾期天数必须 > 0
  AND (
      (br.status = 2 AND DATEDIFF(br.return_time, br.due_time) > 0)
      OR (br.status IN (0, 1, 3) AND DATEDIFF(NOW(), br.due_time) > 0)
  );

-- ============================================================
-- 2. 查看生成的罚金汇总
-- ============================================================
SELECT
    '罚金数据填充完成' AS message,
    COUNT(*) AS total_fine_records,
    COALESCE(SUM(fine_amount), 0) AS total_fine_amount,
    SUM(CASE WHEN payment_status = 0 THEN 1 ELSE 0 END) AS unpaid_count,
    SUM(CASE WHEN payment_status = 1 THEN 1 ELSE 0 END) AS paid_count
FROM fine_record
WHERE is_deleted = 0;
