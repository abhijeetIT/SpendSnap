package com.spendsnap.Controllers;

import com.spendsnap.DTO.CategoryDTO;
import com.spendsnap.DTO.ExpenseDTO;
import com.spendsnap.Entities.Category;
import com.spendsnap.Entities.Expense;
import com.spendsnap.Entities.User;
import com.spendsnap.Services.*;
import com.spendsnap.Util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserRoot {


    private static final Logger log = LoggerFactory.getLogger(UserRoot.class);
    @Autowired
    private UserServices userServices;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private IncomeServices incomeServices;

    @Autowired
    private ExpenseServices expenseServices;

    @Autowired
    private categoryService categoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long id = userUtil.getLoggedInUserId();

        //for user info
        User user = userServices.getUserObject(id);
        model.addAttribute("user", user);

        //for all categories
        model.addAttribute("categories", categoryService.allCategory());

        //current month all expense
        model.addAttribute("monthlyExpenses", expenseServices.currentMonthExpense(id));

        List<Expense> recentExpenses = expenseServices.recentExpense5(id);
        model.addAttribute("recentExpenses", recentExpenses);

        //for expense addition
        Expense expense = new Expense();
        expense.setCategory(new Category());
        expense.setUser(user);

        model.addAttribute("expense", expense);

        return "UserPages/dashboard";
    }

    @GetMapping("/profile")
    public String userProfile(Model model) {
        Long id = userUtil.getLoggedInUserId();

        //for user info
        User user = userServices.getUserObject(id);
        model.addAttribute("user", user);

        //for total transaction
        Long totalTransactions = expenseServices.totalTransaction(id);
        model.addAttribute("totalTransactions", totalTransactions);
        return "UserPages/profile";
    }

    @GetMapping("/transactions")
    public String getTransactions(
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(name = "categoryId", required = false, defaultValue = "0") Long categoryId,
            @RequestParam(name = "dateFilter", required = false, defaultValue = "all") String dateFilter,
            Model model) {

        Long id = userUtil.getLoggedInUserId();
        User user = userServices.getUserObject(id);
        model.addAttribute("user", user);
        model.addAttribute("categories", categoryService.allCategory());

        Page<Expense> pageTransaction = expenseServices.getFilteredExpenses(id, categoryId, dateFilter, currentPage - 1, size);

        model.addAttribute("expenses", pageTransaction.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", pageTransaction.getTotalPages());

        Expense expense = new Expense();
        expense.setCategory(new Category());
        expense.setUser(user);

        model.addAttribute("expense", expense);

        return "UserPages/transactions";
    }

//
//    @PostMapping("/income/add")
//    public String addIncome(@ModelAttribute("income") Income income,
//                            RedirectAttributes redirectAttributes) {
//
//        // Additional safety check - ensure user is set
//        if (income.getUser() == null || income.getUser().getId() == null) {
//            Long userId = userUtil.getLoggedInUserId();
//            User user = userServices.getUserObject(userId);
//            income.setUser(user);
//        }
//
//        if(incomeServices.addIncome(income)) {
//            redirectAttributes.addFlashAttribute("alertType", "success");
//            redirectAttributes.addFlashAttribute("alertMessage", "Income added successfully!");
//        } else {
//            redirectAttributes.addFlashAttribute("alertType", "danger");
//            redirectAttributes.addFlashAttribute("alertMessage", "Failed to add income");
//        }
//        return "redirect:/user/transactions";
//    }

    @PostMapping("/expenses/add")
    public String addExpense(@ModelAttribute("expense") Expense expense,
                             RedirectAttributes redirectAttributes) {

        // Additional safety check for expense too
        if (expense.getUser() == null || expense.getUser().getId() == null) {
            Long userId = userUtil.getLoggedInUserId();
            User user = userServices.getUserObject(userId);
            expense.setUser(user);
        }

        if (expenseServices.addExpense(expense)) {
            redirectAttributes.addFlashAttribute("alertType", "success");
            redirectAttributes.addFlashAttribute("alertMessage", "Expense added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("alertType", "danger");
            redirectAttributes.addFlashAttribute("alertMessage", "Failed to add expense");
        }
        return "redirect:/user/transactions";
    }

    @PostMapping("expenses/update/{id}")
    public String updateExpense(Model model, @PathVariable Long id, @ModelAttribute("expense") Expense expense, RedirectAttributes redirectAttributes) {

        // Additional safety check for expense too
        Expense existingExpense = expenseServices.getExpense(id);
        if (existingExpense == null) {
            redirectAttributes.addFlashAttribute("alertType", "danger");
            redirectAttributes.addFlashAttribute("alertMessage", "Expense not found or you don't have permission");
            return "redirect:/user/transactions";
        }

        try {
            // Update only the fields that should be changed
            existingExpense.setAmount(expense.getAmount());
            existingExpense.setDescription(expense.getDescription());
            existingExpense.setDate(expense.getDate());
            existingExpense.setCategory(expense.getCategory());

            expenseServices.updateExpense(existingExpense);

            redirectAttributes.addFlashAttribute("alertType", "success");
            redirectAttributes.addFlashAttribute("alertMessage", "Expense updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertType", "danger");
            redirectAttributes.addFlashAttribute("alertMessage", "Error updating expense: " + e.getMessage());
        }
        return "redirect:/user/transactions";
    }

    @DeleteMapping("/transaction/expenses/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteExpense(
            @PathVariable Long id,
            @RequestHeader("X-CSRF-TOKEN") String csrfToken
    ) {
        Map<String, String> response = new HashMap<>();

        // Spring Security auto-validates CSRF via the token
        if (expenseServices.deleteExpense(id)) {
            response.put("status", "success");
            response.put("message", "Expense deleted successfully!");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Failed to delete expense");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/categories")
    public String showCategoriesPage(Model model, RedirectAttributes redirectAttributes) {
        // Get the authenticated user from session
        Long id = userUtil.getLoggedInUserId();

        if (id == null) {
            redirectAttributes.addFlashAttribute("alertType", "danger");
            redirectAttributes.addFlashAttribute("alertMessage", "Please login to access categories");
            return "redirect:/login";
        }

        try {
            // Get categories with expense data for the current user
            List<CategoryDTO> categories = categoryService.getCategoriesWithExpenseData(id);

            // Get category expenses map for the modal
            Map<Long, List<ExpenseDTO>> categoryExpenses = categoryService.getCategoryExpensesMap(id);

            // Add data to the model
            model.addAttribute("categories", categories);
            model.addAttribute("categoryExpenses", categoryExpenses);
            model.addAttribute("user", userServices.getUserObject(id)); // convert in dto

            return "UserPages/categories"; // This should match your Thymeleaf template name

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertType", "danger");
            redirectAttributes.addFlashAttribute("alertMessage", "Error loading categories: " + e.getMessage());
            return "redirect:/user/dashboard";
        }

    }
}