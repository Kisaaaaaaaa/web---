package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.entity.Announcement;
import com.campus.bookborrow.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService service;

    @GetMapping
    public Result<?> getVisible() { return Result.ok(service.getVisible()); }

    @GetMapping("/all")
    public Result<?> getAll(@RequestAttribute("roleCode") String roleCode) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        return Result.ok(service.getAll());
    }

    @PostMapping
    public Result<?> add(@RequestAttribute("roleCode") String roleCode,
                         @RequestBody Announcement a) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        service.add(a); return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@RequestAttribute("roleCode") String roleCode,
                            @PathVariable Long id, @RequestBody Announcement a) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        a.setId(id); service.update(a); return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestAttribute("roleCode") String roleCode, @PathVariable Long id) {
        if (!"ROLE_ADMIN".equals(roleCode)) return Result.error(403, "无权限");
        service.delete(id); return Result.ok("删除成功");
    }
}
