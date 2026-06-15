package com.campus.bookborrow.service;

import com.campus.bookborrow.dto.BookDetailVO;
import java.util.List;
import java.util.Map;

public interface FavoriteService {
    /** 切换收藏（收藏/取消收藏），返回是否已收藏 */
    boolean toggleFavorite(Long userId, Long bookId);
    /** 获取用户收藏列表 */
    List<BookDetailVO> getUserFavorites(Long userId);
    /** 检查是否已收藏 */
    boolean isFavorited(Long userId, Long bookId);
}
