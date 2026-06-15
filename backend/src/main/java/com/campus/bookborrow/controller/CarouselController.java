package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.entity.Carousel;
import com.campus.bookborrow.service.CarouselService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carousels")
@RequiredArgsConstructor
public class CarouselController {
    private final CarouselService service;

    @GetMapping
    public Result<?> getEnabled() { return Result.ok(service.getEnabled()); }

    @GetMapping("/all")
    public Result<?> getAll(@RequestAttribute("roleCode") String roleCode) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        return Result.ok(service.getAll());
    }

    @PostMapping
    public Result<?> add(@RequestAttribute("roleCode") String roleCode,
                         @RequestBody Carousel c) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        service.add(c); return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@RequestAttribute("roleCode") String roleCode,
                            @PathVariable Long id, @RequestBody Carousel c) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        c.setId(id); service.update(c); return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestAttribute("roleCode") String roleCode, @PathVariable Long id) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        service.delete(id); return Result.ok("删除成功");
    }
}
