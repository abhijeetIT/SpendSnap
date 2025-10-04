package com.spendsnap.Controllers;

import com.spendsnap.Entities.Category;
import com.spendsnap.Entities.User;
import com.spendsnap.Services.categoryService;
import com.spendsnap.Services.CloudinaryService;
import com.spendsnap.Services.ExpenseServices;
import com.spendsnap.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("ADMIN")
public class AdminController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private categoryService categoryService;

    @Autowired
    private ExpenseServices expenseServices;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String q,
                            Model model) {
        int size = 10; // users per page

        Page<User> users = (q != null && !q.isEmpty())
                ? userServices.searchUsers(q, page, size)
                : userServices.getUsers(page, size);

        model.addAttribute("users", users.getContent());
        model.addAttribute("totalUsers", userServices.totalUser());
        model.addAttribute("activeUsers", userServices.totalActiveUser());
        model.addAttribute("inactiveUsers", userServices.totalInactiveUser());
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("pageNumber", page);

        return "AdminPages/AdminDashboard";
    }

    // Toggle active
    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id, Model model) {
        if(userServices.toggleActive(id)) {
            model.addAttribute("alertMessage", "User status updated!");
            model.addAttribute("alertType", "success");
        }else {
            model.addAttribute("alertMessage", "Something went wrong.!");
            model.addAttribute("alertType", "danger");
        }
        return "redirect:/adminSpendSnap/dashboard";
    }

    // Toggle role
    @PostMapping("/users/{id}/role")
    public String toggleUserRole(@PathVariable Long id, Model model) {
        if (userServices.toggleRole(id)) {
            model.addAttribute("alertMessage", "User role updated!");
            model.addAttribute("alertType", "success");
        }else {
            model.addAttribute("alertMessage", "Something went wrong.!");
            model.addAttribute("alertType", "danger");
        }
        return "redirect:/adminSpendSnap/dashboard";
    }


    @GetMapping("/categories")
    public String categoriesControl(Model model){

        model.addAttribute("totalCategories", categoryService.totalCategory());
        List<Category> categoryList = categoryService.allCategory();

        model.addAttribute("categories",categoryList);
        return "AdminPages/categoriesAdmin";
    }

    @PostMapping("/categories-add")
    public String createCategory(
            @RequestParam String name,
            @RequestParam(value = "iconFile", required = false) MultipartFile iconFile,
            RedirectAttributes redirectAttributes) {

        try {
            Category category = new Category();
            category.setName(name);

            if (iconFile != null && !iconFile.isEmpty()) {
                // Upload to Cloudinary under "CategoryIcon" folder
                String imageUrl = cloudinaryService.categoryIconUpload(iconFile);
                category.setIcon(imageUrl);
            }

            categoryService.addCategory(category);

            redirectAttributes.addFlashAttribute("alertMessage", "Category created successfully");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/adminSpendSnap/categories";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "Error creating category: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/adminSpendSnap/categories";
        }
    }

    // Delete category
    @DeleteMapping("/categories/{categoryId}")
    public String deleteCategory(
            @PathVariable Long categoryId,
            RedirectAttributes redirectAttributes) {

        try {
            // First check if category is used in any expenses
            Long usageCount = expenseServices.totalExpenseInCategoryId(categoryId);

            if (usageCount > 0) {
                redirectAttributes.addFlashAttribute("alertMessage",
                        "Cannot delete category: It is being used in " + usageCount + " expense(s)");
                redirectAttributes.addFlashAttribute("alertType", "warning");
                return "redirect:/adminSpendSnap/categories";
            }

            categoryService.delete(categoryId);
            redirectAttributes.addFlashAttribute("alertMessage", "Category deleted successfully");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/adminSpendSnap/categories";

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("alertMessage",
                    "Cannot delete category: It is being used in other records");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/adminSpendSnap/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "Error deleting category: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/adminSpendSnap/categories";
        }
    }
}
