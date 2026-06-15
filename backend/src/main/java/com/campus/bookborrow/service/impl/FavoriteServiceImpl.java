package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.dto.BookDetailVO;
import com.campus.bookborrow.mapper.UserFavoriteMapper;
import com.campus.bookborrow.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper favoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long userId, Long bookId) {
        if (favoriteMapper.countByUserAndBook(userId, bookId) > 0) {
            favoriteMapper.delete(userId, bookId);
            return false; // 已取消
        } else {
            var fav = new com.campus.bookborrow.entity.UserFavorite();
            fav.setUserId(userId);
            fav.setBookId(bookId);
            favoriteMapper.insert(fav);
            return true; // 已收藏
        }
    }

    @Override
    public List<BookDetailVO> getUserFavorites(Long userId) {
        return favoriteMapper.selectUserFavorites(userId);
    }

    @Override
    public boolean isFavorited(Long userId, Long bookId) {
        return favoriteMapper.countByUserAndBook(userId, bookId) > 0;
    }
}
