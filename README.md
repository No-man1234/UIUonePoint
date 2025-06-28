# UIUonePOINT

UIUonePOINT is an Integrated Management System for handling both Canteen and Store (Grocery) operations within United International University (UIU). The system provides a unified platform for managing food orders and grocery purchases, with separate interfaces for customers and administrators.
---
## Teammates

- **Abdullah Al Noman** (https://github.com/No-man1234)
- **Kazi Neyamul Hasan** (https://github.com/neyamulhasan)
- **Mahathir Mohammad** (https://github.com/mahathir58)


---

## Table of Contents
- [Features](#features)
- [Technical Stack](#technical-stack)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Configuration](#database-configuration)
- [Running & Testing](#running--testing)
- [Project Structure](#project-structure)
- [Module/Class Overview](#moduleclass-overview)
- [User Roles](#user-roles)
- [Troubleshooting & FAQ](#troubleshooting--faq)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### Canteen Section
- View and order from the canteen menu
- Track food order history
- Real-time order notifications
- Daily sales reports
- Menu management (Admin)
- Order status tracking

### Grocery Store Section
- Browse grocery items by category
- Purchase grocery items
- View purchase history
- Inventory management (Admin)
- Low stock alerts
- Sales and inventory reports

### EShopping Section
- Browse and search eShopping products by category (Electronics, Clothing, Books, Accessories, Others)
- View product details, including images, descriptions, and seller info
- Place orders for eShopping products with quantity and mobile number
- Track eShopping order status
- Chat with seller while the order is not delivered yet
- Sellers can add, edit, and manage their own products
- Sellers can view and process eShopping orders
- Product stock management and low stock alerts for sellers
- Filter and search products and orders

### Chat System
- Real-time chat between customers and sellers for eShopping orders
- Integrated chat window accessible from order management screens
- Username-based chat sessions
- Simple, user-friendly interface
- **Note:** To use the chat system, you must first run the chat server before starting the main project.

### General Features
- User authentication (Customer/Admin/Seller roles)
- Section selection (Canteen/Store/eShopping)
- Real-time inventory updates
- Secure data management
- Responsive JavaFX interface

## Technical Stack

- **Backend**: Java 17+
- **Frontend**: JavaFX
- **Database**: MySQL
- **Build Tool**: Maven
- **Other**: FXML for UI layouts, JDBC for database connectivity

## System Architecture

- **MVC Pattern**: The application follows the Model-View-Controller (MVC) architecture.
- **JavaFX**: Used for building the user interface.
- **DAO Layer**: Handles all database operations.
- **Utility Classes**: For image caching, scheduling, and database connections.
- **FXML**: UI layouts are defined in FXML files for separation of design and logic.

```
[User] <-> [JavaFX UI (View)] <-> [Controller] <-> [Model/DAO] <-> [MySQL Database]
```

## Prerequisites

- JDK 17 or higher
- JavaFX SDK (matching your JDK version)
- MySQL Server
- Maven
- (Optional) IDE: IntelliJ IDEA, Eclipse, or VS Code

## Installation

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd UIUonePOINT
   ```

2. **Database Setup**
   - Install MySQL Server
   - Create a database named `uiuonepoint`
   - Import the schema from `src/main/resources/schema.sql` (or your provided schema)
   - Example (MySQL CLI):
     ```sql
     CREATE DATABASE uiuonepoint;
     USE uiuonepoint;
     SOURCE path/to/schema.sql;
     ```

3. **Configure Database Connection**
   - Edit the database connection settings in `DatabaseConnection.java` (typically in `src/main/java/com/example/uiuonepoint/util/DatabaseConnection.java`).
   - Set your MySQL username, password, and database URL.

4. **Build and Run**
   ```bash
   mvn clean install
   mvn javafx:run
   ```
   Or, run from your IDE using the main class: `com.example.uiuonepoint.UIUonePOINTApplication`

## Database Configuration

- Ensure MySQL is running and accessible.
- Update credentials in the code if needed.
- The schema file should create all necessary tables for users, orders, items, etc.
- For development, you can use a local MySQL instance; for production, configure remote access as needed.

## Running & Testing

- **Run the Chat Server First:**
  - Move the UIUonePoint-Server folder from the project directory and place it somewhere else 
  - Navigate to the chat server directory (e.g., `src/main/java/com/example/uiuonepoint/server/chatserver/`).
  - Compile and run the chat server (a `main` method in a `ChatServer.java` file).
  - Ensure the chat server is running and listening on the correct port (default: 5000).
- **Now back to the Project Directory** 
- **Run via Maven**: `mvn javafx:run`
- **Run via IDE**: Right-click `UIUonePOINTApplication.java` and select 'Run'.
- **Testing**: Manual testing via the UI. (Automated tests can be added as needed.)
- **Stopping**: Close the application window or stop the process in your IDE/terminal.

## Project Structure

```


UIUonePOINT/
│
├── pom.xml
├── README.md
├── mvnw / mvnw.cmd
├── .gitignore
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── uiuonepoint/
│       │               │
│       │               ├── controller/      # JavaFX controllers for all UI screens
│       │               │   ├── AdminBaseController.java
│       │               │   ├── AdminCanteenOrdersController.java
│       │               │   ├── AdminDashboardController.java
│       │               │   ├── AdminGroceryOrdersController.java
│       │               │   ├── AdminSellerRequestsController.java
│       │               │   ├── CanteenViewController.java
│       │               │   ├── ChatController.java
│       │               │   ├── CustomerBaseController.java
│       │               │   ├── CustomerOrdersController.java
│       │               │   ├── EShoppingOrdersController.java
│       │               │   ├── EShoppingProductDetailsController.java
│       │               │   ├── EShoppingProductsManagerController.java
│       │               │   ├── EShoppingViewController.java
│       │               │   ├── FoodItemsManagerController.java
│       │               │   ├── GroceryItemsManagerController.java
│       │               │   ├── GroceryViewController.java
│       │               │   ├── ItemDetailsController.java
│       │               │   ├── LoginController.java
│       │               │   ├── MyOrdersController.java
│       │               │   ├── SectionSelectorController.java
│       │               │   ├── SellerBaseController.java
│       │               │   ├── SellerDashboardController.java
│       │               │   ├── SignupController.java
│       │               │
│       │               ├── dao/             # Data Access Objects for DB operations
│       │               │   ├── DAO.java
│       │               │   ├── EShoppingOrderDAO.java
│       │               │   ├── EShoppingProductDAO.java
│       │               │   ├── FoodItemDAO.java
│       │               │   ├── FoodOrderDAO.java
│       │               │   ├── GroceryItemDAO.java
│       │               │   ├── GroceryOrderDAO.java
│       │               │   ├── SellerRequestDAO.java
│       │               │   ├── UserDAO.java
│       │               │
│       │               ├── model/              # Data models/entities
│       │               │   ├── EShoppingOrder.java
│       │               │   ├── EShoppingProduct.java
│       │               │   ├── FoodItem.java
│       │               │   ├── FoodOrder.java
│       │               │   ├── FoodOrderItem.java
│       │               │   ├── GroceryItem.java
│       │               │   ├── GroceryOrder.java
│       │               │   ├── GroceryOrderItem.java
│       │               │   ├── SellerRequest.java
│       │               │   ├── User.java
│       │               │
│       │               ├── util/               # Utility classes
│       │               │   ├── CartManager.java
│       │               │   ├── ChatLauncher.java
│       │               │   ├── DatabaseConnection.java
│       │               │   ├── ImageCache.java
│       │               │   ├── ImageUtils.java
│       │               │   ├── ImgBBUploader.java
│       │               │   ├── OrderSchedulerService.java
│       │               │
│       │               ├── UIUonePOINTApplication.java   # Main entry point
│       │               ├── AI_instructions.md
│       │               ├── UIUonePOINT_project_requirements.md
│       │
│       ├── resources/
│       │   └── com/
│       │       └── example/
│       │           └── uiuonepoint/
│       │               │
│       │               ├── FXML files for all screens:
│       │               │   ├── admin-canteen-orders.fxml
│       │               │   ├── admin-dashboard.fxml
│       │               │   ├── admin-grocery-orders.fxml
│       │               │   ├── admin-seller-requests.fxml
│       │               │   ├── canteen-view.fxml
│       │               │   ├── chat-view.fxml
│       │               │   ├── customer-orders.fxml
│       │               │   ├── eshopping-orders.fxml
│       │               │   ├── eshopping-product-details.fxml
│       │               │   ├── eshopping-products-manager.fxml
│       │               │   ├── eshopping-view.fxml
│       │               │   ├── food-items-manager.fxml
│       │               │   ├── grocery-items-manager.fxml
│       │               │   ├── grocery-view.fxml
│       │               │   ├── hello-view.fxml
│       │               │   ├── item-details.fxml
│       │               │   ├── login.fxml
│       │               │   ├── my-orders.fxml
│       │               │   ├── section-selector.fxml
│       │               │   ├── seller-dashboard.fxml
│       │               │   ├── signup.fxml
│       │               │   ├── styles.css
│       │               │
│       │               ├── images/             # UI and default images
│       │               │   ├── default-item.png
│       │               │   ├── loginView.jpg
│       │               │   ├── loginView.png
│       │               │
│       │               ├── uploads/            # Uploaded product images
│       │               │   ├── 35e1f170-02d5-4a1a-82e4-d483db7f3f41.jpg
│       │               │   ├── 884c5ce4-b3cd-4615-a408-343490ecf275.jpg
│       │               │   ├── 9444ff50-eeb2-48b8-9ae8-4ddb154a1ed0.jpeg
│       │               │   ├── a0cbf96b-aa2c-4fc7-b2fc-7e640b0bb9fc.jpg
│       │               │   ├── f38eddfd-8fe3-464e-a9b0-3533345b622a.jpeg
│       │               │
│       │
│       ├── module-info.java
│
├── target/                                   # Build output (generated) 
```

## Module/Class Overview

- **controller/**: Handles UI logic for each screen (e.g., login, orders, admin dashboard, eShopping views).
- **model/**: Contains data classes representing users, items, orders, and eShopping products/orders.
- **dao/**: Data Access Objects for CRUD operations with the database, including eShopping products and orders.
- **util/**: Utility classes for image caching, database connections, scheduling, etc.
- **resources/**: FXML files for UI layouts, images, and stylesheets, including eShopping views.
- **UIUonePOINTApplication.java**: Main entry point for the JavaFX application.
- **controller/ChatController.java**: Handles the chat UI and communication logic for real-time messaging between users.
- **util/ChatLauncher.java**: Utility to launch the chat window and initialize chat sessions.
- **resources/chat-view.fxml**: FXML layout for the chat window.

## User Roles

### Customer
- Place food orders
- Purchase grocery items
- Place eShopping orders and chat with the seller
- View order history (food, grocery, eShopping)
- Track order status

### Seller
- Add and manage eShopping products
- View and process eShopping orders
- Monitor product stock and receive low stock alerts
- View sales reports

### Admin
- Manage food menu
- Handle grocery inventory
- View sales reports
- Process orders
- Monitor stock levels
- Manages Seller Requests

## Troubleshooting & FAQ

**Q: JavaFX not found or not launching?**
- Ensure JavaFX SDK is installed and configured in your IDE or build tool.
- Check your `pom.xml` for JavaFX dependencies.

**Q: Database connection errors?**
- Verify MySQL is running and credentials in `DatabaseConnection.java` are correct.
- This project uses the MySql in "4306" port instead of regular "3306" port.
- Ensure the schema is imported and tables exist.

**Q: UI not displaying correctly?**
- Make sure FXML files are in the correct resource path.
- Check for missing images or resources.

**Q: How to reset the database?**
- Drop and recreate the `uiuonepoint` database, then re-import the schema.

**Q: How to add new features or sections?**
- Follow the MVC pattern: add new models, controllers, DAOs, and FXML views as needed.

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License. 
