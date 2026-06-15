package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 切换收藏 */
    @PostMapping("/toggle/{bookId}")
    public Result<?> toggle(@RequestAttribute("userId") Long userId,
                            @PathVariable Long bookId) {
        boolean favorited = favoriteService.toggleFavorite(userId, bookId);
        return Result.ok(favorited ? "已收藏" : "已取消收藏", favorited);
    }

    /** 获取用户收藏列表 */
    @GetMapping
    public Result<?> list(@RequestAttribute("userId") Long userId) {
        return Result.ok(favoriteService.getUserFavorites(userId));
    }

    /** 检查是否已收藏 */
    @GetMapping("/check/{bookId}")
    public Result<?> check(@RequestAttribute("userId") Long userId,
                           @PathVariable Long bookId) {
        return Result.ok(favoriteService.isFavorited(userId, bookId));
    }
}
