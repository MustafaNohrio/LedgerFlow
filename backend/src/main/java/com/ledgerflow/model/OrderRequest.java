package com.ledgerflow.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

// DTO for placing a new order from the frontend.
// The frontend sends: which customer, optional notes, and a list of products + quantities.
// The backend then validates stock, calculates totals, and creates the actual Order entity.
@Data
public class OrderRequest {
    @NotNull private Long customerId;
    private String remarks;

    // At least one product must be in the order — you can't place an empty order
    @NotEmpty
    private List<ItemLine> items;

    // Each line = one product and how many units the customer wants
    @Data
    public static class ItemLine {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;
    }
}
