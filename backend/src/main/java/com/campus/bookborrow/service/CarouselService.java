package com.campus.bookborrow.service;

import com.campus.bookborrow.entity.Carousel;
import java.util.List;

public interface CarouselService {
    List<Carousel> getEnabled();
    List<Carousel> getAll();
    void add(Carousel c);
    void update(Carousel c);
    void delete(Long id);
}
