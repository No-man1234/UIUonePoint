

### **Project Requirements Document: UIUonePOINT**

UIUonePOINT is an Integrated Management System for handling both Canteen and Store (Grocery) operations within an organization. It allows users to interact with either section depending on their needs, with distinct features for **Customers** and **Admins**. This project uses **Java** for backend logic, **JavaFX** for the GUI, and **MySQL** for data storage.

---

### ✅ **System Overview:**
Upon login, users choose between two sections:
- 🥘 **Canteen**
- 🛒 **Grocery Store**

---

### 👥 **User Roles:**
1. **Customer:** Can view, order, and manage food and grocery items.
2. **Admin:** Can manage inventory, view reports, and handle order processing for both canteen and store.

---

### 🥘 **Section 1: Canteen Functional Requirements**

| **Req ID** | **Description**                | **User Story**                                                                                      | **Expected Behavior/Outcome**                                                                                                   |
|-----------|--------------------------------|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| C-FR001   | Canteen Menu Display            | As a customer, I want to view available food items with prices and descriptions.                    | System shows a menu with all food items, prices, availability, and category (breakfast/lunch/snacks).                          |
| C-FR002   | Place Food Order                | As a customer, I want to select food items and place an order.                                      | System allows item selection, quantity, adds to cart, and places order. Order is stored in DB.                                 |
| C-FR003   | View Food Order History         | As a customer, I want to view my past food orders.                                                  | System displays order history with status (pending/ready/delivered).                                                           |
| C-FR004   | Manage Food Items (Admin)       | As an admin, I want to add, update, or delete food items.                                           | Admin panel allows full CRUD for food items. Changes reflect in customer view immediately.                                     |
| C-FR005   | Real-Time Food Order Alerts     | As an admin, I want to be notified when a new order is placed.                                      | System triggers a JavaFX alert or notification to the canteen admin dashboard.                                                 |
| C-FR006   | Daily Canteen Sales Report      | As an admin, I want to see sales reports to track revenue and food popularity.                      | Generates date-wise reports showing total sales, best-sellers, etc., fetched from MySQL.                                       |

---

### 🛒 **Section 2: Grocery Store Functional Requirements**

| **Req ID** | **Description**                | **User Story**                                                                                      | **Expected Behavior/Outcome**                                                                                                   |
|-----------|--------------------------------|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| G-FR001   | Browse Grocery Items            | As a customer, I want to view all available grocery items with details.                             | System lists grocery products by category (snacks, stationery, etc.) with prices, stock info.                                  |
| G-FR002   | Purchase Grocery Items          | As a customer, I want to select and buy grocery items.                                               | Allows adding items to cart and finalizing order. Quantity updates in the database.                                             |
| G-FR003   | View Grocery Purchase History   | As a customer, I want to see my previous grocery purchases.                                          | Displays past purchases with dates, quantities, and cost.                                                                       |
| G-FR004   | Manage Grocery Inventory (Admin)| As an admin, I want to manage grocery stock, add/edit/remove items.                                 | Inventory panel allows stock updates and product entry/removal. Stock reflects in customer view.                               |
| G-FR005   | Inventory Alert System          | As an admin, I want to receive low-stock alerts for grocery items.                                  | System highlights low-stock items and can show alerts when quantity falls below threshold.                                     |
| G-FR006   | Grocery Sales & Inventory Report| As an admin, I want to generate sales and stock reports.                                             | Generates PDF/CSV reports for item usage, sales history, and restock needs using MySQL queries.                                |

---

### 🔒 **General System Requirements (Applies to Both Sections)**

| **Req ID** | **Description**                     | **User Story**                                                                 | **Expected Behavior/Outcome**                                                                                                  |
|-----------|-------------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| GEN-FR001 | User Registration/Login              | As a user, I want to register or log in securely.                              | Login screen supports role-based login (customer/admin). Auth checks performed.                                               |
| GEN-FR002 | Role-Based Access                    | As a user, I should see only features relevant to my role.                     | Admins get dashboard access, customers get shopping/order interface.                                                          |
| GEN-FR003 | Section Selection at Startup         | As a user, I want to choose whether to use the Canteen or Grocery section.     | After login, a section selector is shown (buttons or dropdown) to enter canteen or store section.                             |
| GEN-FR004 | GUI Responsiveness with JavaFX       | As a user, I want a smooth, clean interface.                                   | UI updates in real-time with scene switching, tables, charts, and input validation.                                           |
| GEN-FR005 | Secure MySQL Integration             | As a developer, I want data securely stored and retrieved from MySQL.          | All data operations are handled with secure queries. Proper error handling and rollback mechanisms implemented.               |
