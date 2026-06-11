-- ============================================================
-- LedgerFlow Sample Data (inserts.sql)
-- ============================================================
-- This script populates the database with initial records so the system
-- is ready to use immediately. 
--
-- IMPORTANT: 
-- 1. Run this script ONLY after schema.sql has been executed.
-- 2. The order of insertion is critical because of Foreign Key constraints.
--    (e.g., you can't add a Product until its Category exists).
-- ============================================================

-- 1. ROLES
-- Defines the access levels for our staff members.
-- seq_role_id.NEXTVAL pulls the next unique number from the sequence.
INSERT INTO roles VALUES (seq_role_id.NEXTVAL, 'ADMIN');
INSERT INTO roles VALUES (seq_role_id.NEXTVAL, 'MANAGER');
INSERT INTO roles VALUES (seq_role_id.NEXTVAL, 'CASHIER');
INSERT INTO roles VALUES (seq_role_id.NEXTVAL, 'SALES_AGENT');
COMMIT;

-- 2. USERS (Staff Accounts)
-- Note: Passwords are encrypted using BCrypt (Blowfish) algorithm.
-- The hashes here correspond to: admin123, manager123, cash123, sales123
INSERT INTO users(user_id, role_id, full_name, username, password_hash, email, is_active)
VALUES(seq_user_id.NEXTVAL, 1, 'Imran Siddiqui', 'admin',
  '$2a$10$ZGb7cEyzkzevKoLa3mobde.0cjXW0BkNOfA1emEtLRVrNHprEwa3C',
  'admin@ledgerflow.pk', 'Y');

INSERT INTO users(user_id, role_id, full_name, username, password_hash, email, is_active)
VALUES(seq_user_id.NEXTVAL, 2, 'Tariq Mehmood', 'manager',
  '$2a$10$.KZU9hBjodM5f/Llys3zHuJBRqKoOgwtkDDsVJ1M3Kce547d/xPtO',
  'tariq@ledgerflow.pk', 'Y');

INSERT INTO users(user_id, role_id, full_name, username, password_hash, email, is_active)
VALUES(seq_user_id.NEXTVAL, 3, 'Sana Iqbal', 'cashier',
  '$2a$10$nwlldB4lzsSV6SS.VzsAjuF29e60vh/FgCg0QUUCDxLZ6gfbaKypy',
  'sana@ledgerflow.pk', 'Y');

INSERT INTO users(user_id, role_id, full_name, username, password_hash, email, is_active)
VALUES(seq_user_id.NEXTVAL, 4, 'Bilal Hussain', 'sales',
  '$2a$10$tIcMCCf66ywv.1n4/23ehuAXNVak/KxYpY9k5bxt2ZiL4cXjThb.O',
  'bilal@ledgerflow.pk', 'Y');
COMMIT;

-- 3. CATEGORIES
-- Groups our products for easier reporting and filtering.
INSERT INTO categories VALUES(seq_category_id.NEXTVAL, 'Electronics', 'Phones, laptops, accessories');
INSERT INTO categories VALUES(seq_category_id.NEXTVAL, 'Clothing',    'Men and women apparel');
INSERT INTO categories VALUES(seq_category_id.NEXTVAL, 'Appliances',  'Kitchen and home machines');
INSERT INTO categories VALUES(seq_category_id.NEXTVAL, 'Books',       'Academic and general reading');
INSERT INTO categories VALUES(seq_category_id.NEXTVAL, 'Sports',      'Fitness and outdoor equipment');
COMMIT;

-- 4. PRODUCTS
-- Sample inventory items. 
-- Note: updated_by_user_id links to the Admin user created above.
INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,1,'Samsung Galaxy A55','SMSG-A55',89999,55,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,1,'Sony WH-1000XM5 Headphones','SONY-WH5',18500,80,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,1,'Lenovo IdeaPad 5 Laptop','LEN-IP5',148000,22,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,2,'Cotton Polo Shirt','POLO-M-BLU',1499,200,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,2,'Slim Fit Chino Pants','CHNO-M-KHK',2799,140,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,3,'Dawlance Microwave 25L','DWL-MW25',22000,38,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,3,'Orient 1.5 Ton Inverter AC','ORT-AC15',96000,9,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,4,'Database Systems - Elmasri','BOOK-DBS',2800,60,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,4,'Clean Code - Robert Martin','BOOK-CC',3200,45,'Y',1);

INSERT INTO products(product_id,category_id,product_name,sku_code,unit_price,stock_quantity,is_active,updated_by_user_id)
VALUES(seq_product_id.NEXTVAL,5,'Wilson Pro Staff Racket','WLS-PSR',8500,30,'Y',1);
COMMIT;

-- 5. CUSTOMERS
-- The people buying from our store.
INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Ahmed','Khan','ahmed.khan@gmail.com','0300-1234567','Karachi');

INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Sara','Ali','sara.ali@gmail.com','0321-9876543','Lahore');

INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Usman','Raza','usman.raza@gmail.com','0333-4455667','Islamabad');

INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Fatima','Sheikh','fatima.sheikh@gmail.com','0345-7788990','Peshawar');

INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Bilal','Mahmood','bilal.m@gmail.com','0312-6655443','Faisalabad');

INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Zainab','Mirza','zainab.mirza@gmail.com','0301-2233445','Multan');

INSERT INTO customers(customer_id,first_name,last_name,email_address,phone_number,city_name)
VALUES(seq_customer_id.NEXTVAL,'Hamza','Qureshi','hamza.q@gmail.com','0311-5544332','Lahore');
COMMIT;

-- 6. ORDERS
-- Initial sales history. 
-- total_amount is 0 here; it gets updated automatically by a DB trigger
-- when we insert the items below.
INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,1,3,DATE '2026-01-08','DELIVERED','First order - express requested');

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,2,4,DATE '2026-01-20','DELIVERED',NULL);

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,3,3,DATE '2026-02-03','DELIVERED','Bulk purchase - office');

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,4,4,DATE '2026-02-18','SHIPPED',NULL);

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,5,3,DATE '2026-03-05','DELIVERED',NULL);

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,6,4,DATE '2026-03-22','DELIVERED',NULL);

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,1,3,DATE '2026-04-10','CONFIRMED',NULL);

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,7,4,DATE '2026-04-15','PENDING',NULL);

INSERT INTO orders(order_id,customer_id,created_by_user_id,order_date,order_status,remarks)
VALUES(seq_order_id.NEXTVAL,2,3,DATE '2026-04-20','PENDING',NULL);
COMMIT;

-- 7. ORDER_ITEMS
-- Linking products to orders. 
-- Every insert here triggers: stock check -> stock deduction -> order total sync.
INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1001,1,1,89999);
INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1001,2,1,18500);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1002,4,3,1499);
INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1002,5,2,2799);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1003,3,1,148000);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1004,6,1,22000);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1005,8,2,2800);
INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1005,9,2,3200);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1006,7,1,96000);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1007,2,1,18500);
INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1007,10,1,8500);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1008,4,4,1499);

INSERT INTO order_items(order_item_id,order_id,product_id,quantity,unit_price)
VALUES(seq_order_item_id.NEXTVAL,1009,3,1,148000);
COMMIT;

-- 8. PAYMENTS
-- Tracking money collected for orders.
INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1001,DATE '2026-01-08',108499,'CARD','COMPLETED');

INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1002,DATE '2026-01-20',10095,'CASH','COMPLETED');

INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1003,DATE '2026-02-03',100000,'BANK_TRANSFER','COMPLETED');

INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1003,DATE '2026-02-10',48000,'BANK_TRANSFER','COMPLETED');

INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1004,DATE '2026-02-18',22000,'ONLINE','COMPLETED');

INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1005,DATE '2026-03-05',12000,'CASH','COMPLETED');

INSERT INTO payments(payment_id,order_id,payment_date,amount_paid,payment_method,payment_status)
VALUES(seq_payment_id.NEXTVAL,1006,DATE '2026-03-22',96000,'CARD','COMPLETED');
COMMIT;
