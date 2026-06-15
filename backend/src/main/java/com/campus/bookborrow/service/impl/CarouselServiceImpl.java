package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.entity.Carousel;
import com.campus.bookborrow.mapper.CarouselMapper;
import com.campus.bookborrow.service.CarouselService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class CarouselServiceImpl implements CarouselService {
    private final CarouselMapper mapper;

    @Override public List<Carousel> getEnabled() { return mapper.selectEnabled(); }
    @Override public List<Carousel> getAll() { return mapper.selectAll(); }
    @Override public void add(Carousel c) { mapper.insert(c); }
    @Override public void update(Carousel c) { mapper.update(c); }
    @Override public void delete(Long id) { mapper.deleteById(id); }
}
