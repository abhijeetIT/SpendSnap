package com.spendsnap.DTO;

public class CategoryDTO {
    private Long id;
    private String name;
    private String icon;
    private Long expenseCount;
    private Double totalAmount;

    // Constructors, getters, and setters
    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String icon, Long expenseCount, Double totalAmount) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.expenseCount = expenseCount;
        this.totalAmount = totalAmount;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Long getExpenseCount() { return expenseCount; }
    public void setExpenseCount(Long expenseCount) { this.expenseCount = expenseCount; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}