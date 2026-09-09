package com.spendsnap.DTO;

/**
 * Response returned to the frontend after an uploaded receipt/bill image
 * has been scanned and parsed by the AI service.
 *
 * NOTE: This DTO only carries *suggested* values. Nothing is saved to the
 * database from this response directly — the user always reviews the
 * pre-filled form and explicitly clicks "Add Expense" to save it.
 */
public class ReceiptScanResponse {

    private boolean success;
    private String message;

    private Double amount;
    private String date;          // yyyy-MM-dd, ready to drop into <input type="date">
    private String description;   // merchant name / short summary, ready for the description field

    private String suggestedCategoryName; // whatever the AI guessed, even if we don't have it
    private Long matchedCategoryId;        // set only if it matched one of the user's existing categories
    private String matchedCategoryName;

    public ReceiptScanResponse() {
    }

    public static ReceiptScanResponse failure(String message) {
        ReceiptScanResponse response = new ReceiptScanResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuggestedCategoryName() {
        return suggestedCategoryName;
    }

    public void setSuggestedCategoryName(String suggestedCategoryName) {
        this.suggestedCategoryName = suggestedCategoryName;
    }

    public Long getMatchedCategoryId() {
        return matchedCategoryId;
    }

    public void setMatchedCategoryId(Long matchedCategoryId) {
        this.matchedCategoryId = matchedCategoryId;
    }

    public String getMatchedCategoryName() {
        return matchedCategoryName;
    }

    public void setMatchedCategoryName(String matchedCategoryName) {
        this.matchedCategoryName = matchedCategoryName;
    }
}
