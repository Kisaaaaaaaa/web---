package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.Carousel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CarouselMapper {
    List<Carousel> selectAll();
    List<Carousel> selectEnabled();
    int insert(Carousel carousel);
    int update(Carousel carousel);
    int deleteById(@Param("id") Long id);
}
