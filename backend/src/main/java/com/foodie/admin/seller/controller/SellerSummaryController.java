
package com.foodie.admin.seller.controller;

import com.foodie.admin.seller.dto.SellerOrderDtos.SummaryView;
import com.foodie.admin.seller.security.SellerAuthInterceptor;
import com.foodie.admin.seller.service.SellerSummaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/summary")
public class SellerSummaryController {

    private final SellerSummaryService summary;

    public SellerSummaryController(SellerSummaryService summary) {
        this.summary = summary;
    }

    @GetMapping
    public SummaryView getSummary(HttpServletRequest request) {
        String sellerId = (String) request.getAttribute(
                SellerAuthInterceptor.SELLER_ID_ATTR);

        return summary.getSummary(sellerId);
    }
}
