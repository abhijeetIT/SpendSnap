package com.spendsnap.Services;

import com.spendsnap.DTO.CategoryDTO;
import com.spendsnap.DTO.ExpenseDTO;
import com.spendsnap.Entities.Category;

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
