-- schema.sql
-- LedgerFlow - Sales Management System
-- Oracle 19c / 21c XE
-- Credentials: ledgerflow / pass123
-- Run this, then inserts.sql

-- ============================================================
-- CLEANUP (uncomment to start fresh)
-- ============================================================
-- DROP TABLE audit_log      CASCADE CONSTRAINTS PURGE;
-- DROP TABLE payments       CASCADE CONSTRAINTS PURGE;
-- DROP TABLE order_items    CASCADE CONSTRAINTS PURGE;
-- DROP TABLE orders         CASCADE CONSTRAINTS PURGE;
-- DROP TABLE customers      CASCADE CONSTRAINTS PURGE;
-- DROP TABLE products       CASCADE CONSTRAINTS PURGE;
-- DROP TABLE categories     CASCADE CONSTRAINTS PURGE;
-- DROP TABLE users          CASCADE CONSTRAINTS PURGE;
-- DROP TABLE roles          CASCADE CONSTRAINTS PURGE;
-- DROP SEQUENCE seq_role_id;
-- DROP SEQUENCE seq_user_id;
-- DROP SEQUENCE seq_category_id;
-- DROP SEQUENCE seq_product_id;
-- DROP SEQUENCE seq_customer_id;
-- DROP SEQUENCE seq_order_id;
-- DROP SEQUENCE seq_order_item_id;
-- DROP SEQUENCE seq_payment_id;
-- DROP SEQUENCE seq_audit_id;

-- ============================================================
-- SEQUENCES
-- Sequences generate unique auto-incrementing IDs for each table.
-- Oracle doesn't have AUTO_INCREMENT like MySQL, so we use sequences instead.
-- Note: Order IDs start from 1001 so they look more professional.
-- ============================================================
CREATE SEQUENCE seq_role_id       START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_user_id       START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_category_id   START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_product_id    START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_customer_id   START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_order_id      START WITH 1001 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_order_item_id START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_payment_id    START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_audit_id      START WITH 1    INCREMENT BY 1 NOCACHE NOCYCLE;

-- ============================================================
-- ROLES
-- ============================================================
CREATE TABLE roles (
    role_id    NUMBER       DEFAULT seq_role_id.NEXTVAL CONSTRAINT pk_roles PRIMARY KEY,
    role_name  VARCHAR2(50) NOT NULL CONSTRAINT uq_role_name UNIQUE
);

-- ============================================================
-- USERS  (staff accounts, not customers)
-- ============================================================
CREATE TABLE users (
    user_id        NUMBER        DEFAULT seq_user_id.NEXTVAL CONSTRAINT pk_users PRIMARY KEY,
    role_id        NUMBER        NOT NULL,
    full_name      VARCHAR2(150) NOT NULL,
    username       VARCHAR2(50)  NOT NULL CONSTRAINT uq_username UNIQUE,
    password_hash  VARCHAR2(255) NOT NULL,
    email          VARCHAR2(255) NOT NULL CONSTRAINT uq_user_email UNIQUE,
    is_active      CHAR(1)       DEFAULT 'Y' CONSTRAINT ck_user_active CHECK (is_active IN ('Y','N')),
    created_at     TIMESTAMP     DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- ============================================================
-- CATEGORIES
-- ============================================================
CREATE TABLE categories (
    category_id    NUMBER        DEFAULT seq_category_id.NEXTVAL CONSTRAINT pk_categories PRIMARY KEY,
    category_name  VARCHAR2(100) NOT NULL CONSTRAINT uq_cat_name UNIQUE,
    description    VARCHAR2(500)
);

-- ============================================================
-- PRODUCTS
-- ============================================================
CREATE TABLE products (
    product_id         NUMBER        DEFAULT seq_product_id.NEXTVAL CONSTRAINT pk_products PRIMARY KEY,
    category_id        NUMBER        NOT NULL,
    product_name       VARCHAR2(200) NOT NULL,
    sku_code           VARCHAR2(50)  NOT NULL CONSTRAINT uq_sku_code UNIQUE,
    unit_price         NUMBER(12,2)  NOT NULL CONSTRAINT ck_unit_price CHECK (unit_price > 0),
    stock_quantity     NUMBER(10)    DEFAULT 0 CONSTRAINT ck_stock CHECK (stock_quantity >= 0),
    is_active          CHAR(1)       DEFAULT 'Y' CONSTRAINT ck_prod_active CHECK (is_active IN ('Y','N')),
    created_at         TIMESTAMP     DEFAULT SYSTIMESTAMP,
    updated_at         TIMESTAMP     DEFAULT SYSTIMESTAMP,
    updated_by_user_id NUMBER,
    CONSTRAINT fk_prod_cat     FOREIGN KEY (category_id)        REFERENCES categories(category_id),
    CONSTRAINT fk_prod_updater FOREIGN KEY (updated_by_user_id) REFERENCES users(user_id)
);

-- ============================================================
-- CUSTOMERS  (buyers, no system login)
-- ============================================================
CREATE TABLE customers (
    customer_id    NUMBER        DEFAULT seq_customer_id.NEXTVAL CONSTRAINT pk_customers PRIMARY KEY,
    first_name     VARCHAR2(100) NOT NULL,
    last_name      VARCHAR2(100) NOT NULL,
    email_address  VARCHAR2(255) CONSTRAINT uq_cust_email UNIQUE,
    phone_number   VARCHAR2(20),
    city_name      VARCHAR2(100),
    created_at     TIMESTAMP     DEFAULT SYSTIMESTAMP,
    updated_at     TIMESTAMP     DEFAULT SYSTIMESTAMP
);

-- ============================================================
-- ORDERS
-- ============================================================
CREATE TABLE orders (
    order_id            NUMBER        DEFAULT seq_order_id.NEXTVAL CONSTRAINT pk_orders PRIMARY KEY,
    customer_id         NUMBER        NOT NULL,
    created_by_user_id  NUMBER        NOT NULL,
    order_date          DATE          DEFAULT SYSDATE,
    order_status        VARCHAR2(20)  DEFAULT 'PENDING'
        CONSTRAINT ck_order_status CHECK (
            order_status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')
        ),
    total_amount        NUMBER(14,2)  DEFAULT 0 CONSTRAINT ck_order_total CHECK (total_amount >= 0),
    paid_amount         NUMBER(14,2)  DEFAULT 0 CONSTRAINT ck_order_paid  CHECK (paid_amount >= 0),
    remarks             VARCHAR2(500),
    updated_at          TIMESTAMP     DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id)        REFERENCES customers(customer_id),
    CONSTRAINT fk_order_user     FOREIGN KEY (created_by_user_id) REFERENCES users(user_id)
);

-- ============================================================
-- ORDER_ITEMS
-- No line_total stored. Use quantity * unit_price in queries.
-- ============================================================
CREATE TABLE order_items (
    order_item_id NUMBER       DEFAULT seq_order_item_id.NEXTVAL CONSTRAINT pk_order_items PRIMARY KEY,
    order_id      NUMBER       NOT NULL,
    product_id    NUMBER       NOT NULL,
    quantity      NUMBER(10)   NOT NULL CONSTRAINT ck_oi_qty   CHECK (quantity > 0),
    unit_price    NUMBER(12,2) NOT NULL CONSTRAINT ck_oi_price CHECK (unit_price > 0),
    CONSTRAINT uq_order_product UNIQUE (order_id, product_id),
    CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)   REFERENCES orders(order_id)   ON DELETE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- ============================================================
-- PAYMENTS
-- customer accessible via order join - no redundant FK here
-- ============================================================
CREATE TABLE payments (
    payment_id     NUMBER        DEFAULT seq_payment_id.NEXTVAL CONSTRAINT pk_payments PRIMARY KEY,
    order_id       NUMBER        NOT NULL,
    payment_date   DATE          DEFAULT SYSDATE,
    amount_paid    NUMBER(14,2)  NOT NULL CONSTRAINT ck_pay_amount CHECK (amount_paid > 0),
    payment_method VARCHAR2(20)  NOT NULL
        CONSTRAINT ck_pay_method CHECK (payment_method IN ('CASH','CARD','ONLINE','BANK_TRANSFER')),
    payment_status VARCHAR2(20)  DEFAULT 'COMPLETED'
        CONSTRAINT ck_pay_status CHECK (payment_status IN ('COMPLETED','PENDING','FAILED','REFUNDED')),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- ============================================================
-- AUDIT_LOG
-- ============================================================
CREATE TABLE audit_log (
    audit_id           NUMBER        DEFAULT seq_audit_id.NEXTVAL CONSTRAINT pk_audit PRIMARY KEY,
    table_name         VARCHAR2(50)  NOT NULL,
    action_type        VARCHAR2(10)  NOT NULL CONSTRAINT ck_audit_action CHECK (action_type IN ('INSERT','UPDATE','DELETE')),
    record_id          NUMBER        NOT NULL,
    changed_by_user_id NUMBER,
    action_date        TIMESTAMP     DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (changed_by_user_id) REFERENCES users(user_id)
);

-- ============================================================
-- INDEXES
-- Indexes speed up searches on frequently queried columns.
-- Without them, the database would have to scan every single row
-- to find what it's looking for (slow). With indexes, it can jump
-- directly to the matching rows (fast).
-- ============================================================
CREATE INDEX idx_prod_cat      ON products(category_id);    -- speed up product-by-category lookups
CREATE INDEX idx_prod_active   ON products(is_active);      -- speed up filtering active products
CREATE INDEX idx_ord_customer  ON orders(customer_id);      -- speed up order-by-customer lookups
CREATE INDEX idx_ord_date      ON orders(order_date);       -- speed up date range filtering
CREATE INDEX idx_ord_status    ON orders(order_status);     -- speed up status filtering
CREATE INDEX idx_oi_order      ON order_items(order_id);    -- speed up getting items for an order
CREATE INDEX idx_oi_product    ON order_items(product_id);  -- speed up product sales lookups
CREATE INDEX idx_pay_order     ON payments(order_id);       -- speed up payment-by-order lookups
CREATE INDEX idx_pay_date      ON payments(payment_date);   -- speed up payment date filtering
CREATE INDEX idx_user_role     ON users(role_id);           -- speed up user-by-role lookups
CREATE INDEX idx_cust_city     ON customers(city_name);     -- speed up city-based filtering

-- ============================================================
-- TRIGGERS
-- Triggers are automated procedures that fire when data changes.
-- They enforce business rules at the database level, so even if
-- someone bypasses the Java backend and runs SQL directly,
-- these rules still apply.
-- ============================================================

-- TRIGGER 1: Stock Validation
-- Fires BEFORE a product is added to an order.
-- If someone tries to order 50 laptops but we only have 30,
-- this trigger blocks the INSERT and throws an error.
CREATE OR REPLACE TRIGGER trg_stock_check
BEFORE INSERT ON order_items
FOR EACH ROW
DECLARE
    v_stock NUMBER;
    v_name  VARCHAR2(200);
BEGIN
    SELECT stock_quantity, product_name INTO v_stock, v_name
    FROM products WHERE product_id = :NEW.product_id;
    IF v_stock < :NEW.quantity THEN
        RAISE_APPLICATION_ERROR(-20101,
            'Not enough stock for "' || v_name || '". Available: ' || v_stock);
    END IF;
END trg_stock_check;
/

-- TRIGGER 2: Automatic Stock Deduction
-- Fires AFTER a product is successfully added to an order.
-- Automatically subtracts the purchased quantity from the product's stock.
-- This means the Java code doesn't have to manually update inventory —
-- the database handles it automatically.
CREATE OR REPLACE TRIGGER trg_stock_deduct
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE products
    SET stock_quantity = stock_quantity - :NEW.quantity, updated_at = SYSTIMESTAMP
    WHERE product_id = :NEW.product_id;
END trg_stock_deduct;
/

-- TRIGGER 3: Auto-Calculate Order Total (Incremental Math)
-- We use incremental addition/subtraction to avoid the "Mutating Table" error (ORA-04091).
-- Instead of summing the whole table, we just update the difference.
CREATE OR REPLACE TRIGGER trg_sync_order_total
AFTER INSERT OR UPDATE OR DELETE ON order_items
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        UPDATE orders SET total_amount = total_amount + (:NEW.quantity * :NEW.unit_price), updated_at = SYSTIMESTAMP
        WHERE order_id = :NEW.order_id;
    ELSIF UPDATING THEN
        UPDATE orders SET total_amount = total_amount - (:OLD.quantity * :OLD.unit_price) + (:NEW.quantity * :NEW.unit_price), updated_at = SYSTIMESTAMP
        WHERE order_id = :NEW.order_id;
    ELSIF DELETING THEN
        UPDATE orders SET total_amount = total_amount - (:OLD.quantity * :OLD.unit_price), updated_at = SYSTIMESTAMP
        WHERE order_id = :OLD.order_id;
    END IF;
END trg_sync_order_total;
/

-- TRIGGER 4: Overpayment Prevention
-- Checks if the new payment would make the total paid exceed the order total.
-- Since we track paid_amount in the orders table, we don't need to query the payments table (no mutation).
CREATE OR REPLACE TRIGGER trg_no_overpayment
BEFORE INSERT ON payments
FOR EACH ROW
DECLARE
    v_total  NUMBER;
    v_paid   NUMBER;
BEGIN
    SELECT total_amount, paid_amount INTO v_total, v_paid 
    FROM orders WHERE order_id = :NEW.order_id;
    
    IF :NEW.amount_paid > (v_total - v_paid) THEN
        RAISE_APPLICATION_ERROR(-20102,
            'Payment exceeds balance. Still owed: ' || (v_total - v_paid));
    END IF;
END trg_no_overpayment;
/

-- TRIGGER 5: Sync Payment Total to Orders
-- Updates the paid_amount in the orders table whenever a payment is confirmed.
CREATE OR REPLACE TRIGGER trg_sync_payment_total
AFTER INSERT OR UPDATE OR DELETE ON payments
FOR EACH ROW
BEGIN
    IF INSERTING AND :NEW.payment_status = 'COMPLETED' THEN
        UPDATE orders SET paid_amount = paid_amount + :NEW.amount_paid WHERE order_id = :NEW.order_id;
    ELSIF UPDATING THEN
        -- Handle status changes or amount changes
        IF :OLD.payment_status = 'COMPLETED' THEN
            UPDATE orders SET paid_amount = paid_amount - :OLD.amount_paid WHERE order_id = :OLD.order_id;
        END IF;
        IF :NEW.payment_status = 'COMPLETED' THEN
            UPDATE orders SET paid_amount = paid_amount + :NEW.amount_paid WHERE order_id = :NEW.order_id;
        END IF;
    ELSIF DELETING AND :OLD.payment_status = 'COMPLETED' THEN
        UPDATE orders SET paid_amount = paid_amount - :OLD.amount_paid WHERE order_id = :OLD.order_id;
    END IF;
END trg_sync_payment_total;
/

-- TRIGGER 6: Audit Trail for Product Changes
-- This is a security/compliance feature. Whenever any product is created,
-- modified, or deleted, this trigger automatically logs the event into
-- the audit_log table with: what happened, which product, who did it, and when.
-- Even if someone changes a price and tries to hide it, the audit trail catches it.
CREATE OR REPLACE TRIGGER trg_audit_products
AFTER INSERT OR UPDATE OR DELETE ON products
FOR EACH ROW
DECLARE
    v_action VARCHAR2(10);
    v_rec_id NUMBER;
    v_usr_id NUMBER;
BEGIN
    IF INSERTING THEN
        v_action := 'INSERT'; v_rec_id := :NEW.product_id; v_usr_id := :NEW.updated_by_user_id;
    ELSIF UPDATING THEN
        v_action := 'UPDATE'; v_rec_id := :NEW.product_id; v_usr_id := :NEW.updated_by_user_id;
    ELSE
        v_action := 'DELETE'; v_rec_id := :OLD.product_id; v_usr_id := :OLD.updated_by_user_id;
    END IF;
    INSERT INTO audit_log(audit_id, table_name, action_type, record_id, changed_by_user_id, action_date)
    VALUES(seq_audit_id.NEXTVAL, 'products', v_action, v_rec_id, v_usr_id, SYSTIMESTAMP);
END trg_audit_products;
/
