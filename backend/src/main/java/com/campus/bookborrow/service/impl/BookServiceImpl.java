package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.dto.BookDetailVO;
import com.campus.bookborrow.entity.BookInfo;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.BookInfoMapper;
import com.campus.bookborrow.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书管理服务实现
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookInfoMapper bookInfoMapper;

    @Override
    public Map<String, Object> listBooks(String keyword, Long categoryId,
                                          Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<BookDetailVO> list = bookInfoMapper.selectBookList(keyword, categoryId, status, offset, pageSize);
        int total = bookInfoMapper.countBookList(keyword, categoryId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public BookDetailVO getBookDetail(Long id) {
        BookDetailVO book = bookInfoMapper.selectBookDetail(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在或已下架");
        }
        return book;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBook(BookInfo book) {
        // ISBN 唯一性校验
        BookInfo exist = bookInfoMapper.selectByIsbn(book.getIsbn());
        if (exist != null) {
            throw new BusinessException(400, "该ISBN图书已存在，请勿重复录入");
        }
        int rows = bookInfoMapper.insertBook(book);
        if (rows == 0) {
            throw new BusinessException(500, "新增图书失败");
        }
        log.info("新图书已录入 → isbn={}, title={}", book.getIsbn(), book.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBook(BookInfo book) {
        BookInfo exist = bookInfoMapper.selectById(book.getId());
        if (exist == null) {
            throw new BusinessException(404, "图书不存在");
        }
        // ISBN 变更时唯一性校验
        if (!exist.getIsbn().equals(book.getIsbn())) {
            BookInfo dup = bookInfoMapper.selectByIsbn(book.getIsbn());
            if (dup != null && !dup.getId().equals(book.getId())) {
                throw new BusinessException(400, "该ISBN与其他图书冲突");
            }
        }
        // 总库存不能小于已借出数量
        int borrowed = exist.getTotalStock() - exist.getCurrentStock();
        if (book.getTotalStock() < borrowed) {
            throw new BusinessException(400,
                    "总库存不能小于当前已借出数量（" + borrowed + "本）");
        }
        // 更新 currentStock
        int stockDiff = book.getTotalStock() - exist.getTotalStock();
        book.setCurrentStock(exist.getCurrentStock() + stockDiff);

        int rows = bookInfoMapper.updateBook(book);
        if (rows == 0) {
            throw new BusinessException(500, "更新图书信息失败");
        }
        log.info("图书信息已更新 → id={}", book.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookStatus(Long id, Integer status) {
        BookInfo exist = bookInfoMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "图书不存在");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态值非法（0=下架, 1=上架）");
        }
        int rows = bookInfoMapper.updateStatus(id, status);
        if (rows == 0) {
            throw new BusinessException(500, "更新图书状态失败");
        }
        log.info("图书状态已更新 → id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        BookInfo exist = bookInfoMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "图书不存在");
        }
        // 检查是否有未归还的借阅记录
        int rows = bookInfoMapper.deleteBook(id);
        if (rows == 0) {
            throw new BusinessException(500, "删除图书失败");
        }
        log.info("图书已删除（软删除） → id={}, title={}", id, exist.getTitle());
    }
}
