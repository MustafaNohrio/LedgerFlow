package com.ledgerflow.service;

import com.ledgerflow.model.Customer;
import com.ledgerflow.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

// Business logic for managing customer records.
// Customers are the buyers — they don't have system logins.
@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepo;

    // Search with optional name and city filters
    public Page<Customer> search(String q, String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepo.search(
            (q == null || q.isBlank()) ? null : q,
            (city == null || city.isBlank()) ? null : city,
            pageable
        );
    }

    public Customer getById(Long id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    // Register a new customer — check for duplicate email first
    public Customer create(Customer c) {
        if (c.getEmailAddress() != null && !c.getEmailAddress().isBlank()) {
            customerRepo.findByEmailAddress(c.getEmailAddress()).ifPresent(existing -> {
                throw new RuntimeException("A customer with that email already exists");
            });
        }
        return customerRepo.save(c);
    }

    // Update customer details — we don't allow email changes here to prevent duplicates
    public Customer update(Long id, Customer updated) {
        Customer existing = getById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setCityName(updated.getCityName());
        existing.setUpdatedAt(LocalDateTime.now());
        return customerRepo.save(existing);
    }
}
