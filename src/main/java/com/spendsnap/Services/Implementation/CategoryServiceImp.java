package com.spendsnap.Services.Implementation;

import com.spendsnap.DTO.CategoryDTO;
import com.spendsnap.DTO.ExpenseDTO;
import com.spendsnap.Entities.Category;
import com.spendsnap.Entities.Expense;
import com.spendsnap.Repositories.CategoryRepository;
import com.spendsnap.Repositories.ExpenseRepository;
import com.spendsnap.Services.categoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImp implements categoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> getCategory(Long id) {
        return categoryRepository.findById(id);
    }

    //for giving list of categories in transaction page
    @Override
    public List<Category> allCategory() {
        return categoryRepository.findAll();
    }


    @Override
    public Long totalCategory() {
        return categoryRepository.count();
    }

    @Override
    public Boolean delete(Long id) {
        try{
            categoryRepository.deleteById(id);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public List<CategoryDTO> getCategoriesWithExpenseData(Long userId) {
        List<Object[]> results = categoryRepository.findCategoriesWithExpenseData(userId);

        return results.stream().map(result -> {
            Category category = (Category) result[0];
            Long expenseCount = (Long) result[1];
            Double totalAmount = (Double) result[2];

            CategoryDTO dto = new CategoryDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setIcon(category.getIcon());
            dto.setExpenseCount(expenseCount);
            dto.setTotalAmount(totalAmount);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<ExpenseDTO>> getCategoryExpensesMap(Long userId) {
        // Get all categories first
        List<Category> categories = categoryRepository.findAll();
        Map<Long, List<ExpenseDTO>> categoryExpensesMap = new HashMap<>();

        for (Category category : categories) {
            List<Expense> expenses = expenseRepository.findByCategoryIdAndUserId(category.getId(), userId);
            List<ExpenseDTO> expenseDTOs = expenses.stream()
                    .map(expense -> {
                        ExpenseDTO dto = new ExpenseDTO();
                        dto.setId(expense.getId());
                        dto.setAmount(expense.getAmount());
                        dto.setDescription(expense.getDescription());
                        dto.setDate(expense.getDate());
                        return dto;
                    })
                    .collect(Collectors.toList());

            categoryExpensesMap.put(category.getId(), expenseDTOs);
        }

        return categoryExpensesMap;
    }
}