# 📊 LedgerFlow — Sales Management System

A full-stack sales management application built with **Java Spring Boot**, **Oracle Database**, and **vanilla HTML/CSS/JS**. It features role-based access control, real-time inventory management through database triggers, and comprehensive sales analytics.

---

## ✨ Features

| Module | Description |
|--------|-------------|
| **Dashboard** | Revenue stats, monthly sales chart, and low-stock alerts |
| **Products** | Searchable product catalog with add/edit, CSV export, and pagination |
| **Customers** | Customer management with order history linking |
| **Orders** | Order builder with status workflow (Pending → Confirmed → Shipped → Delivered) |
| **Payments** | Payment recording with overpayment protection via DB triggers |
| **Reports** | Monthly sales, top products, customer history, payment method breakdown |
| **User Management** | Admin-only user CRUD with role assignment and account activation |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.2, Spring Data JPA |
| **Database** | Oracle 19c / 21c XE |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **Authentication** | BCrypt password hashing + HTTP sessions |
| **Build Tool** | Apache Maven |

---

## 📁 Project Structure

```
ledgerflow/
├── backend/                        # Spring Boot application
│   └── src/main/java/com/ledgerflow/
│       ├── config/                 # DataSeeder, SessionHelper
│       ├── controller/             # REST API endpoints
│       ├── model/                  # JPA entity classes
│       ├── repository/             # Data access layer (custom SQL queries)
│       ├── service/                # Business logic layer
│       ├── WebConfig.java          # CORS + root redirect config
│       └── LedgerflowApplication.java
│   └── src/main/resources/
│       ├── static/                 # Frontend files served by Spring Boot
│       └── application.properties  # Database & server config
├── frontend/                       # Source frontend files (HTML/CSS/JS)
├── database/
│   ├── schema.sql                  # Tables, sequences, indexes, triggers
│   └── inserts.sql                 # Sample data
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** ([Download](https://adoptium.net/))
- **Apache Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Oracle Database 19c / 21c XE** ([Download](https://www.oracle.com/database/technologies/xe-downloads.html))

### Step 1 — Create the Oracle User

Connect as `SYSDBA` in SQL Developer or SQL*Plus:

```sql
CREATE USER ledgerflow IDENTIFIED BY pass123;
GRANT CONNECT, RESOURCE, CREATE SESSION TO ledgerflow;
GRANT UNLIMITED TABLESPACE TO ledgerflow;
```

### Step 2 — Run the Schema

Connect as `ledgerflow / pass123` and execute:

```
database/schema.sql
```

This creates **9 tables**, **9 sequences**, **indexes**, and **5 triggers**.

Verify:
```sql
SELECT table_name FROM user_tables ORDER BY 1;
```
> Expected: `AUDIT_LOG`, `CATEGORIES`, `CUSTOMERS`, `ORDER_ITEMS`, `ORDERS`, `PAYMENTS`, `PRODUCTS`, `ROLES`, `USERS`

### Step 3 — Load Sample Data

Execute:
```
database/inserts.sql
```

### Step 4 — Configure the Backend

Edit `backend/src/main/resources/application.properties` if your Oracle setup differs:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=ledgerflow
spring.datasource.password=pass123
```

> **Note:** If your Oracle service name is `XE` instead of `XEPDB1`, update the URL accordingly.

### Step 5 — Run the Application

```bash
cd backend
mvn spring-boot:run
```

The app starts at **http://localhost:8080** — it will automatically redirect to the login page.

---

## 🔐 Demo Accounts

| Username | Password | Role | Access Level |
|----------|----------|------|-------------|
| `admin` | `admin123` | ADMIN | Full access — users, reports, all modules |
| `manager` | `manager123` | MANAGER | Products, orders, reports, customers |
| `cashier` | `cash123` | CASHIER | Orders and payments |
| `sales` | `sales123` | SALES_AGENT | Customers, view orders |

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | User login |
| `POST` | `/api/auth/logout` | User logout |
| `GET` | `/api/auth/me` | Get current session info |
| `POST` | `/api/auth/register` | Register new user (Admin only) |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products?q=&categoryId=&active=&page=&size=` | List with filters |
| `POST` | `/api/products` | Create product |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Deactivate product |
| `GET` | `/api/products/export/csv` | Export as CSV |

### Customers
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/customers?q=&city=&page=&size=` | List with filters |
| `POST` | `/api/customers` | Create customer |
| `PUT` | `/api/customers/{id}` | Update customer |
| `GET` | `/api/customers/export/csv` | Export as CSV |

### Orders & Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/orders?status=&from=&to=&customerId=&page=&size=` | List with filters |
| `POST` | `/api/orders` | Place new order |
| `PATCH` | `/api/orders/{id}/status` | Update order status |
| `GET` | `/api/orders/export/csv` | Export as CSV |
| `GET` | `/api/orders/dashboard` | Dashboard stats |
| `POST` | `/api/payments` | Record a payment |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/reports/monthly-sales` | Monthly revenue data |
| `GET` | `/api/reports/top-products` | Top selling products |
| `GET` | `/api/reports/low-stock` | Low stock alerts |
| `GET` | `/api/reports/customer-history` | Customer purchase summary |
| `GET` | `/api/reports/payment-methods` | Payment method breakdown |
| `GET` | `/api/reports/monthly-sales/csv` | Export monthly sales CSV |

---

## 🔄 Order Status Flow

```
PENDING  →  CONFIRMED  →  SHIPPED  →  DELIVERED
  ↓              ↓
CANCELLED    CANCELLED
```

> Cancellation is blocked after `DELIVERED`. Invalid transitions return a descriptive error.

---

## 🗃 Database Design

### Tables (9)

`roles` · `users` · `categories` · `products` · `customers` · `orders` · `order_items` · `payments` · `audit_log`

### Triggers (5)

| Trigger | Event | Purpose |
|---------|-------|---------|
| `trg_stock_check` | `BEFORE INSERT` on `order_items` | Rejects order if stock is insufficient |
| `trg_stock_deduct` | `AFTER INSERT` on `order_items` | Automatically deducts stock after order |
| `trg_sync_order_total` | `AFTER INSERT/UPDATE/DELETE` on `order_items` | Keeps `orders.total_amount` in sync |
| `trg_no_overpayment` | `BEFORE INSERT` on `payments` | Blocks payment exceeding the balance due |
| `trg_audit_products` | `AFTER INSERT/UPDATE/DELETE` on `products` | Logs all product changes to `audit_log` |

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| `ORA-12541: No listener` | Start Oracle service: `Services → OracleServiceXE → Start` |
| `ORA-01017: invalid credentials` | Check username/password in `application.properties` |
| `Table does not exist` | Run `schema.sql` before starting the backend |
| Login fails with correct password | Re-run `inserts.sql` after a fresh schema for correct BCrypt hashes |
| `ORA-00955: name already used` | Uncomment the `DROP` statements at the top of `schema.sql` |
| Whitelabel Error Page (404) | Make sure you visit `http://localhost:8080/login.html` |

---

## 📜 License

This project is for educational purposes.
