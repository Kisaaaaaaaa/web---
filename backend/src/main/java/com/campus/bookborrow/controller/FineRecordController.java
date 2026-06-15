package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.service.FineRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 罚金管理控制器
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-15
 */
@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineRecordController {

    private final FineRecordService fineRecordService;

    /** 分页条件查询罚金记录 */
    @GetMapping
    public Result<?> listFineRecords(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("roleCode") String roleCode,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Long filterUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 管理员可查看全部，学生只能看自己的
        Long queryUserId = "ROLE_ADMIN".equals(roleCode) ? filterUserId : userId;
        return Result.ok(fineRecordService.listFineRecords(
                queryUserId, username, paymentStatus, page, pageSize));
    }

    /** 查询罚金详情 */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.ok(fineRecordService.getById(id));
    }

    /** 更新缴费状态（管理员操作） */
    @PutMapping("/{id}/payment")
    public Result<?> updatePaymentStatus(@RequestAttribute("roleCode") String roleCode,
                                          @PathVariable Long id,
                                          @RequestBody Map<String, Integer> body) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        Integer status = body.get("paymentStatus");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "缴费状态值非法（0=未缴, 1=已缴）");
        }
        fineRecordService.updatePaymentStatus(id, status);
        return Result.ok(status == 1 ? "已标记为已缴纳" : "已标记为未缴纳");
    }

    /** 获取用户未缴罚金总额 */
    @GetMapping("/unpaid-total")
    public Result<?> getUnpaidTotal(@RequestAttribute("userId") Long userId,
                                     @RequestAttribute("roleCode") String roleCode) {
        Long queryUserId = "ROLE_ADMIN".equals(roleCode) ? null : userId;
        return Result.ok(fineRecordService.getUnpaidFineTotal(queryUserId));
    }

    /** 罚金状态分布统计 */
    @GetMapping("/stats/distribution")
    public Result<?> getFineStatusDistribution() {
        return Result.ok(fineRecordService.getFineStatusDistribution());
    }
}
