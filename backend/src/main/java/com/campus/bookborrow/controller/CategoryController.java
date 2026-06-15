package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.entity.BookCategory;
import com.campus.bookborrow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理控制器
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 获取分类树（公开接口） */
    @GetMapping("/tree")
    public Result<?> getCategoryTree() {
        return Result.ok(categoryService.getCategoryTree());
    }

    /** 获取所有分类（平铺） */
    @GetMapping
    public Result<?> listAll() {
        return Result.ok(categoryService.listAll());
    }

    /** 新增分类（管理员） */
    @PostMapping
    public Result<?> addCategory(@RequestAttribute("roleCode") String roleCode,
                                  @RequestBody BookCategory category) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        categoryService.addCategory(category);
        return Result.ok("新增分类成功");
    }

    /** 更新分类（管理员） */
    @PutMapping("/{id}")
    public Result<?> updateCategory(@RequestAttribute("roleCode") String roleCode,
                                     @PathVariable Long id,
                                     @RequestBody BookCategory category) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.ok("更新分类成功");
    }

    /** 删除分类（管理员） */
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@RequestAttribute("roleCode") String roleCode,
                                     @PathVariable Long id) {
        if (!"ROLE_ADMIN".equals(roleCode)) {
            return Result.error(403, "无操作权限");
        }
        categoryService.deleteCategory(id);
        return Result.ok("删除分类成功");
    }
}
