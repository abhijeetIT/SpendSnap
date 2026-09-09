# 💸 SpendSnap — Smart Expense Manager

> A **Spring Boot + MySQL** based Expense Management and Tracking Application designed for young users (ages 17–35) to monitor, control, and optimize daily spending.  
> Built with performance, scalability, and clean architecture in mind. 🚀

![GitHub stars](https://img.shields.io/github/stars/abhijeetIT/SpendSnap?style=social)
![GitHub forks](https://img.shields.io/github/forks/abhijeetIT/SpendSnap?style=social)
![GitHub license](https://img.shields.io/github/license/abhijeetIT/SpendSnap)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Cloudinary](https://img.shields.io/badge/Cloud-Cloudinary-lightblue)

🌐 **For Live Demo Click on =>:** [SpendSnap](https://spendsnap.koyeb.app/)

---

## 🧠 Overview  

**SpendSnap** is a user-friendly expense tracking application built for modern users to gain better control over their financial habits.  
It allows users to record, categorize, and visualize their spending — all from a secure, responsive web platform.  

---

## ⚙️ Key Features  

✅ **AI-Powered Receipt Scanning** — Snap or upload a photo of a bill and let AI auto-fill the amount, date, description, and category. No more manual typing — just review and save. Powered by Google Gemini (free tier).  
✅ **User & Category Management** — Add, edit, and organize expenses by category.  
✅ **Admin Panel** — Custom-built admin dashboard for managing users and categories.  
✅ **Secure Database Layer** — Built with Hibernate + Spring Data JPA for reliability.  
✅ **DTO Layer Integration** — Ensures data privacy and structured response handling.  
✅ **Cloudinary Integration** — For safe and optimized image uploads.  
✅ **Custom Exception Handling** — Using `@ControllerAdvice` and `HttpExceptionHandler` for clean debugging.  
✅ **Optimized Performance** —  
- Handles up to **65% traffic stress** under load tests.  
- **Latency:** 0.30 microseconds for near real-time updates.  

---

## 🤖 AI Receipt Scanning — How It Works

Instead of typing every expense by hand, SpendSnap can read it for you straight off a photo of the bill.

1. **Snap a Photo** — On the Dashboard or Transactions page, tap the camera button (or "Scan Receipt") and take/upload a picture of a receipt.
2. **AI Reads It** — The image is sent to Google's Gemini vision model, which extracts the total amount, date, merchant, and suggests the best-fitting category.
3. **Review & Save** — The Add Expense form is pre-filled with everything the AI found. You review it (and fix anything if needed) and hit **Add Expense** to save — nothing is saved automatically without your confirmation.

If the AI can't confidently match one of your existing categories, the category field is simply left blank for you to pick manually — it never guesses your data into the database.

### Setup (required to enable this feature)

1. Get a **free** Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Set it as an environment variable named `GEMINI_API_KEY` (same way you already set `MYSQLUSER`, `CLOUDINARY_API_KEY`, `BREVO_API_KEY`, etc.).
3. That's it — no extra Maven dependency was needed; the app talks to Gemini directly over `RestTemplate`.

> ⚠️ Google occasionally retires older Gemini model versions. If receipt scanning suddenly starts failing with a `404 model not found` error, open `application.properties` and update the model name in `gemini.api.url` to whatever Google's error message recommends as the replacement (see [Gemini model deprecations](https://ai.google.dev/gemini-api/docs/deprecations) for the current list).

---

## 🛠️ Tech Stack  

| Layer | Technology |
|:------|:------------|
| 🧩 **Backend Framework** | Spring Boot (v3.x) |
| 🗄️ **Database** | MySQL |
| 🌿 **ORM** | Hibernate + Spring Data JPA |
| ☁️ **Cloud Storage** | Cloudinary |
| 🤖 **AI / Vision** | Google Gemini API (receipt scanning) |
| 🧱 **Architecture** | DTO Pattern + Layered MVC |
| 🔐 **Security** | Spring Security (optional setup ready) |
| 🧰 **Build Tool** | Maven |

---
🧑‍💻 Developer

👨‍💻 Name: Abhijeet Jha
🎓 Course: BCA (3rd Semester)
💼 Aspiration: Backend Developer | Java & Spring Boot Enthusiast

🌐 Connect With Me

📧 Email: abhijeetj4324@gmail.com

💼 LinkedIn: https://www.linkedin.com/in/abhijeet-jha19

🌍 GitHub: @abhijeetIT

📸 Instagram: @_abhijeet_jha_

