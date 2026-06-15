package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.entity.BookCategory;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.BookCategoryMapper;
import com.campus.bookborrow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类管理服务实现
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final BookCategoryMapper categoryMapper;

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<BookCategory> allCategories = categoryMapper.selectAll();
        // 组装树形结构：先找顶级分类，再递归挂子分类
        List<Map<String, Object>> tree = new ArrayList<>();
        for (BookCategory cat : allCategories) {
            if (cat.getParentId() == 0) {
                Map<String, Object> node = categoryToMap(cat);
                node.put("children", buildChildren(allCategories, cat.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    private List<Map<String, Object>> buildChildren(List<BookCategory> all, Long parentId) {
        List<Map<String, Object>> children = new ArrayList<>();
        for (BookCategory cat : all) {
            if (cat.getParentId().equals(parentId)) {
                Map<String, Object> node = categoryToMap(cat);
                node.put("children", buildChildren(all, cat.getId()));
                children.add(node);
            }
        }
        return children;
    }

    private Map<String, Object> categoryToMap(BookCategory cat) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", cat.getId());
        map.put("categoryName", cat.getCategoryName());
        map.put("parentId", cat.getParentId());
        map.put("sortOrder", cat.getSortOrder());
        return map;
    }

    @Override
    public List<BookCategory> listAll() {
        return categoryMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(BookCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        int rows = categoryMapper.insertCategory(category);
        if (rows == 0) {
            throw new BusinessException(500, "新增分类失败");
        }
        log.info("新分类已创建 → name={}", category.getCategoryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(BookCategory category) {
        BookCategory exist = categoryMapper.selectById(category.getId());
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        int rows = categoryMapper.updateCategory(category);
        if (rows == 0) {
            throw new BusinessException(500, "更新分类失败");
        }
        log.info("分类已更新 → id={}", category.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        BookCategory exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        // 检查是否有子分类
        List<BookCategory> children = categoryMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(400, "该分类下存在子分类，无法删除");
        }
        int rows = categoryMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(500, "删除分类失败");
        }
        log.info("分类已删除 → id={}, name={}", id, exist.getCategoryName());
    }
}
