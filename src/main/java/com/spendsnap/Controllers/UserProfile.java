package com.spendsnap.Controllers;


import com.spendsnap.Entities.User;
import com.spendsnap.Services.CloudinaryService;
import com.spendsnap.Services.UserServices;
import com.spendsnap.Util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/profile")
public class UserProfile {

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private UserServices userServices;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/update")
    public String profileUpdate(Model model, @ModelAttribute("user") User user, RedirectAttributes re) {
        Long id = userUtil.getLoggedInUserId();
        User existingUser = userServices.getUserObject(id);
        //set a value to fetched by html
        // Validate input
        if (user.getName() == null || user.getEmail() == null) {
            re.addFlashAttribute("errorMessage", "Name and Email are required!");
            return "redirect:/user/profile";
        }
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setDateOfBirth(user.getDateOfBirth());

        if (userServices.updateUser(existingUser)) {
            re.addFlashAttribute("successMessage", "Profile updated successfully!");
        } else {
            re.addFlashAttribute("errorMessage", "Failed to update account.Please try later.");
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/delete")
    public String profileDelete(RedirectAttributes re) {

        Long id = userUtil.getLoggedInUserId();
        User deleteUser = userServices.getUserObject(id);

        deleteUser.setActive(false);
        if (userServices.updateUser(deleteUser)) {
//            re.addFlashAttribute("successMessage",
//                    "Account deactivated! Contact support within 15 days to recover.");
            return "redirect:/user/profile/delete-page";
        } else {
            re.addFlashAttribute("errorMessage", "Failed to delete account.");
            return "redirect:/user/profile";
        }
    }

    @PostMapping("/change-password")
    public String changePass(@RequestParam("currentPassword") String currentPassword,
                             @RequestParam("newPassword") String newPassword,
                             @RequestParam("confirmPassword") String confirmPassword,
                             RedirectAttributes re) {

        try {
            // 1. Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                re.addFlashAttribute("errorMessage", "New passwords do not match!");
                return "redirect:/user/profile";
            }

            // 2. Get current user
            User existingUser = userServices.getUserObject(userUtil.getLoggedInUserId());

            // 3. Verify current password
            if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
                re.addFlashAttribute("errorMessage", "Current password is incorrect!");
                return "redirect:/user/profile";
            }

            // 4. Check if new password is same as old password
            if (passwordEncoder.matches(newPassword, existingUser.getPassword())) {
                re.addFlashAttribute("errorMessage", "New password cannot be the same as old password!");
                return "redirect:/user/profile";
            }

            // 5. Update password
            existingUser.setPassword(passwordEncoder.encode(newPassword));
            userServices.updateUser(existingUser);

            // 6. Success - stay on profile page with success message
            re.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/user/profile";

        } catch (Exception e) {
            re.addFlashAttribute("errorMessage", "Failed to change password. Please try again.");
            return "redirect:/user/profile";
        }
    }

    @GetMapping("/delete-page")
    public String deletePage(Model model) {
        model.addAttribute("userName", userServices.getUserObject(userUtil.getLoggedInUserId()).getName());
        return "Authentication/delete";
    }

    @PostMapping("/update-picture")
    public String uploadPicture(@RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
                                RedirectAttributes re) {
        Long id = userUtil.getLoggedInUserId();

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                // Validate file type
                String contentType = profilePicture.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    re.addFlashAttribute("errorMessage", "Please select a valid image file.");
                    return "redirect:/user/profile";
                }

                // Validate file size (e.g., 5MB max)
                if (profilePicture.getSize() > 10 * 1024 * 1024) {
                    re.addFlashAttribute("errorMessage", "Image size must be less than 10MB.");
                    return "redirect:/user/profile";
                }

                User existUser = userServices.getUserObject(id);

                // Delete old image if it exists
                if(existUser.getProfilePictureUrl() != null && !existUser.getProfilePictureUrl().isEmpty()) {
                    cloudinaryService.deleteDataImage(existUser.getProfilePictureUrl());
                }

                // Upload new image
                String imagUrl = cloudinaryService.uploadPicture(profilePicture);
                existUser.setProfilePictureUrl(imagUrl);
                userServices.updateUser(existUser);

                re.addFlashAttribute("successMessage", "Image uploaded successfully!");
                return "redirect:/user/profile";

            } catch (Exception e) {
                re.addFlashAttribute("errorMessage", "Failed to upload image. Please try again.");
                return "redirect:/user/profile";
            }
        } else {
            re.addFlashAttribute("errorMessage", "Please select an image to upload.");
            return "redirect:/user/profile";
        }
    }
}
