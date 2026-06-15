package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AnnouncementMapper {
    List<Announcement> selectAll();
    List<Announcement> selectVisible();
    int insert(Announcement ann);
    int update(Announcement ann);
    int deleteById(@Param("id") Long id);
}
