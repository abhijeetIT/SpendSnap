package com.spendsnap.service;

import com.spendsnap.dto.CategoryDTO;
import com.spendsnap.dto.ExpenseDTO;
import com.spendsnap.entity.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface categoryService {
    Category addCategory(Category category);
    Optional<Category> getCategory(Long id);
    List<Category> allCategory();
    Long totalCategory();
    Boolean delete(Long id);

    // for category pages
    List<CategoryDTO> getCategoriesWithExpenseData(Long userId);
    Map<Long, List<ExpenseDTO>> getCategoryExpensesMap(Long userId);
}
