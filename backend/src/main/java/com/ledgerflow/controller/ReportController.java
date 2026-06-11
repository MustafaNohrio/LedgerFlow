package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

// API endpoints for the Reports page — all the analytical data.
// Only ADMIN and MANAGER roles can access reports.
// Each endpoint returns data from a complex SQL query in OrderRepository.
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private ReportService reportService;

    // GET /api/reports/monthly-sales — revenue grouped by month for the current year
    @GetMapping("/monthly-sales")
    public ResponseEntity<?> monthlySales(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(buildRows(reportService.getMonthlySales(),
            "month", "monthNum", "orderCount", "revenue"));
    }

    // GET /api/reports/top-products — best-selling products ranked by units sold
    @GetMapping("/top-products")
    public ResponseEntity<?> topProducts(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(buildRows(reportService.getTopSellingProducts(),
            "productName", "skuCode", "categoryName", "totalUnits", "totalRevenue"));
    }

    // GET /api/reports/low-stock — products running low on inventory
    @GetMapping("/low-stock")
    public ResponseEntity<?> lowStock(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(buildRows(reportService.getLowStockReport(),
            "productName", "skuCode", "categoryName", "stockQuantity", "unitPrice"));
    }

    // GET /api/reports/customer-history — customers ranked by total spending
    @GetMapping("/customer-history")
    public ResponseEntity<?> customerHistory(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(buildRows(reportService.getCustomerPurchaseHistory(),
            "customerName", "city", "totalOrders", "totalSpent", "lastOrderDate"));
    }

    // GET /api/reports/payment-methods — breakdown of Cash vs Card vs Online payments
    @GetMapping("/payment-methods")
    public ResponseEntity<?> paymentMethods(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(buildRows(reportService.getPaymentMethodSummary(),
            "paymentMethod", "txnCount", "totalAmount"));
    }

    // GET /api/reports/monthly-sales/csv — download monthly sales as a CSV file
    @GetMapping("/monthly-sales/csv")
    public void exportMonthlySalesCsv(HttpSession session, HttpServletResponse response) throws IOException {
        if (SessionHelper.requireRole(session, "ADMIN", "MANAGER") != null) {
            response.setStatus(403); return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"monthly-sales.csv\"");
        PrintWriter w = response.getWriter();
        w.println("Month,Orders,Revenue");
        for (Object[] r : reportService.getMonthlySales()) {
            w.printf("%s,%s,%.2f%n", r[0], r[2], ((Number) r[3]).doubleValue());
        }
    }

    // Helper: converts raw Object[] rows from native SQL into clean JSON-friendly Maps.
    // Without this, the frontend would get unnamed arrays like [["Jan", 5, 1200]].
    // With this, it gets named objects like [{"month": "Jan", "orderCount": 5, "revenue": 1200}].
    private List<Map<String, Object>> buildRows(List<Object[]> rows, String... keys) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < keys.length && i < row.length; i++) {
                map.put(keys[i], row[i]);
            }
            result.add(map);
        }
        return result;
    }
}
