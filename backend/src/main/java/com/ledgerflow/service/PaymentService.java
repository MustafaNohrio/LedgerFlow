package com.ledgerflow.service;

import com.ledgerflow.model.*;
import com.ledgerflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

// Handles payment recording and overpayment prevention.
// Also auto-confirms an order once it's been fully paid.
@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepo;
    @Autowired private OrderRepository   orderRepo;

    public List<Payment> getAll() { return paymentRepo.findAll(); }

    // Get all payments made against a specific order
    public List<Payment> getByOrder(Long orderId) {
        return paymentRepo.findByOrderOrderId(orderId);
    }

    // Record a new payment — with several safety checks
    @Transactional
    public Payment record(Long orderId, BigDecimal amount, String method) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order #" + orderId + " not found"));

        // Can't pay for a cancelled order — makes no sense
        if ("CANCELLED".equals(order.getOrderStatus()))
            throw new RuntimeException("Cannot accept payment for a cancelled order");

        // Calculate how much is still owed
        BigDecimal alreadyPaid = paymentRepo.getTotalPaidForOrder(orderId);
        BigDecimal balance = order.getTotalAmount().subtract(alreadyPaid);

        // Basic validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Payment amount must be greater than zero");

        // Prevent overpayment — can't pay more than what's owed
        if (amount.compareTo(balance) > 0)
            throw new RuntimeException(
                "Payment of " + amount + " exceeds the balance due of " + balance);

        // Create and save the payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmountPaid(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus("COMPLETED");

        Payment saved = paymentRepo.save(payment);

        // Smart feature: if this payment completes the full amount and the order
        // is still PENDING, automatically move it to CONFIRMED
        if (alreadyPaid.add(amount).compareTo(order.getTotalAmount()) >= 0
                && "PENDING".equals(order.getOrderStatus())) {
            order.setOrderStatus("CONFIRMED");
            orderRepo.save(order);
        }

        return saved;
    }
}
