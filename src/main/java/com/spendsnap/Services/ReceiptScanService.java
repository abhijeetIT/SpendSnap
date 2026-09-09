package com.spendsnap.Services;

import com.spendsnap.DTO.ReceiptScanResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptScanService {

    /**
     * Sends the uploaded receipt/bill image to the AI vision model and
     * returns the extracted amount, date, description and a best-guess
     * category — matched against the user's own categories where possible.
     */
    ReceiptScanResponse scanReceipt(MultipartFile receiptImage, Long userId);
}
