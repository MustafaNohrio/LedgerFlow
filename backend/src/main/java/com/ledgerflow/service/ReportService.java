package com.ledgerflow.service;

import com.ledgerflow.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Thin service layer for the Reports page.
// All the heavy SQL work is done in OrderRepository — this just delegates the calls.
// Each method corresponds to a specific report tab on the frontend.
@Service
public class ReportService {

    @Autowired private OrderRepository orderRepo;

    // Monthly sales chart — groups orders by month for the current year
    public List<Object[]> getMonthlySales() {
        return orderRepo.getMonthlySales();
    }

    // Top selling products — ranked by total units sold
    public List<Object[]> getTopSellingProducts() {
        return orderRepo.getTopSellingProductsReport();
    }

    // Low stock alert — products with 10 or fewer units remaining
    public List<Object[]> getLowStockReport() {
        return orderRepo.getLowStockReport();
    }

    // Customer purchase history — ranked by total spending
    public List<Object[]> getCustomerPurchaseHistory() {
        return orderRepo.getCustomerPurchaseHistory();
    }

    // Payment method breakdown — Cash vs Card vs Online etc.
    public List<Object[]> getPaymentMethodSummary() {
        return orderRepo.getPaymentMethodSummary();
    }
}
