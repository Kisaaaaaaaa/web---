package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 借阅控制器
 * 处理借书、还书、续借、借阅记录查询
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /** 借阅记录查询（含动态罚金） */
    @GetMapping
    public Result<?> listBorrowRecords(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roleCode") String roleCode,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long filterUserId,
            @RequestParam(required = false) String bookKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 管理员可查看全部，学生只能看自己的
        Long queryUserId = "ROLE_ADMIN".equals(roleCode) ? filterUserId : userId;
        return Result.ok(borrowService.listBorrowRecords(queryUserId, username, status, bookKeyword, page, pageSize));
    }

    /** 借书 */
    @PostMapping
    public Result<?> borrowBook(@RequestAttribute("userId") Long userId,
                                 @RequestParam Long bookId) {
        borrowService.borrowBook(userId, bookId);
        return Result.ok("借阅成功，请于30天内归还");
    }

    /** 归还图书 */
    @PutMapping("/{id}/return")
    public Result<?> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return Result.ok("归还成功");
    }

    /** 续借 */
    @PutMapping("/{id}/renew")
    public Result<?> renewBook(@PathVariable Long id) {
        borrowService.renewBook(id);
        return Result.ok("续借成功，归还日期已延长30天");
    }

    /** 管理员手动标记逾期 */
    @PutMapping("/{id}/overdue")
    public Result<?> markOverdue(@RequestAttribute("roleCode") String roleCode,
                                  @PathVariable Long id) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        borrowService.markOverdue(id);
        return Result.ok("已标记为逾期");
    }

    /** 统计用户借阅数据 */
    @GetMapping("/stats")
    public Result<?> getStats(@RequestAttribute("userId") Long userId,
                               @RequestAttribute("roleCode") String roleCode) {
        Long queryUserId = "ROLE_ADMIN".equals(roleCode) ? null : userId;
        var stats = borrowService.getUserBorrowStats(queryUserId);
        stats.put("overdueCount", borrowService.getUserOverdueCount(userId));
        return Result.ok(stats);
    }

    /** 借阅排行榜 */
    @GetMapping("/top")
    public Result<?> getTopBooks(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(borrowService.getTopBooks(limit));
    }

    /** 各分类借阅数量统计 */
    @GetMapping("/stats/category")
    public Result<?> getStatsByCategory() {
        return Result.ok(borrowService.getBorrowCountByCategory());
    }

    /** 近7天每日借阅与逾期统计 */
    @GetMapping("/stats/daily")
    public Result<?> getDailyStats() {
        return Result.ok(borrowService.getDailyBorrowStats());
    }

    /** 借阅状态分布统计 */
    @GetMapping("/stats/distribution")
    public Result<?> getStatusDistribution() {
        return Result.ok(borrowService.getStatusDistribution());
    }
}
