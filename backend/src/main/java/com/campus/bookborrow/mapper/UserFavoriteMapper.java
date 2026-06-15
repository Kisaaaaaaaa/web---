package com.campus.bookborrow.mapper;

import com.campus.bookborrow.dto.BookDetailVO;
import com.campus.bookborrow.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFavoriteMapper {

    /** 添加收藏 */
    int insert(UserFavorite fav);

    /** 取消收藏 */
    int delete(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /** 查询用户是否已收藏某书 */
    int countByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /** 查询用户所有收藏（联查图书信息） */
    List<BookDetailVO> selectUserFavorites(@Param("userId") Long userId);

    /** 收藏总数 */
    int countUserFavorites(@Param("userId") Long userId);
}
