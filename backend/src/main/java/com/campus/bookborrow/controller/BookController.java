package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.entity.BookInfo;
import com.campus.bookborrow.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 图书控制器
 * 提供图书查询和管理接口
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /** 分页条件查询图书列表 */
    @GetMapping
    public Result<Map<String, Object>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        return Result.ok(bookService.listBooks(keyword, categoryId, status, page, pageSize));
    }

    /** 查询图书详情 */
    @GetMapping("/{id}")
    public Result<?> getBookDetail(@PathVariable Long id) {
        return Result.ok(bookService.getBookDetail(id));
    }

    /** 新增图书（管理员） */
    @PostMapping
    public Result<?> addBook(@RequestAttribute("roleCode") String roleCode,
                              @RequestBody BookInfo book) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        bookService.addBook(book);
        return Result.ok("新增图书成功");
    }

    /** 更新图书信息（管理员） */
    @PutMapping("/{id}")
    public Result<?> updateBook(@RequestAttribute("roleCode") String roleCode,
                                 @PathVariable Long id,
                                 @RequestBody BookInfo book) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        book.setId(id);
        bookService.updateBook(book);
        return Result.ok("更新图书信息成功");
    }

    /** 上架/下架图书（管理员） */
    @PutMapping("/{id}/status")
    public Result<?> updateBookStatus(@RequestAttribute("roleCode") String roleCode,
                                       @PathVariable Long id,
                                       @RequestParam Integer status) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        bookService.updateBookStatus(id, status);
        return Result.ok(status == 1 ? "图书已上架" : "图书已下架");
    }
}
