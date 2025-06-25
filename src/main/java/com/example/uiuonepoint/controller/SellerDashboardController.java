package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.FoodOrderDAO;
import com.example.uiuonepoint.dao.GroceryOrderDAO;
import com.example.uiuonepoint.dao.EShoppingProductDAO;
import com.example.uiuonepoint.dao.EShoppingOrderDAO;
import com.example.uiuonepoint.model.User;
import com.example.uiuonepoint.model.EShoppingProduct;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;

public class SellerDashboardController extends SellerBaseController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Text pendingOrdersCount;
    @FXML private Text todaySales;
    @FXML private Text totalProductsCount;
    @FXML private Text lowStockCount;
    @FXML private TableView<EShoppingProduct> productsTable;
    @FXML private TableColumn<EShoppingProduct, Integer> idColumn;
    @FXML private TableColumn<EShoppingProduct, String> nameColumn;
    @FXML private TableColumn<EShoppingProduct, Double> priceColumn;
    @FXML private TableColumn<EShoppingProduct, Integer> stockColumn;
    
    private final FoodOrderDAO foodOrderDAO = new FoodOrderDAO();
    private final GroceryOrderDAO groceryOrderDAO = new GroceryOrderDAO();
    private final EShoppingProductDAO eshoppingProductDAO = new EShoppingProductDAO();
    private final EShoppingOrderDAO eshoppingOrderDAO = new EShoppingOrderDAO();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProductsTable();
        // Set up a timer to refresh data every 30 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> loadData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void setupProductsTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
    }
    
    @Override
    protected void onInitialize() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Seller)");
            loadData();
        }
    }
    
    private void loadData() {
        if (currentUser == null) return;
        
        try {
            // Get pending orders count
            int pendingFoodOrders = foodOrderDAO.getPendingOrdersCount();
            int pendingGroceryOrders = groceryOrderDAO.getPendingOrdersCount();
            int pendingEShoppingOrders = eshoppingOrderDAO.getPendingOrdersCount(currentUser.getId());
            int totalPendingOrders = pendingFoodOrders + pendingGroceryOrders + pendingEShoppingOrders;
            pendingOrdersCount.setText(String.valueOf(totalPendingOrders));

            // Calculate today's sales
            double todayFoodSalesAmount = foodOrderDAO.getTodaySales();
            double todayGrocerySalesAmount = groceryOrderDAO.getTodaySales();
            double todayEShoppingSalesAmount = eshoppingOrderDAO.getTodaySales(currentUser.getId());
            double totalSales = todayFoodSalesAmount + todayGrocerySalesAmount + todayEShoppingSalesAmount;
            
            // Format sales with Bengali Taka symbol (৳)
            todaySales.setText(String.format("৳%.2f", totalSales));

            // Load eShopping products
            List<EShoppingProduct> products = eshoppingProductDAO.getBySellerId(currentUser.getId());
            productsTable.setItems(FXCollections.observableArrayList(products));
            
            // Update product counts
            totalProductsCount.setText(String.valueOf(products.size()));
            long lowStockCount = products.stream()
                .filter(p -> p.getStockQuantity() <= 5)
                .count();
            this.lowStockCount.setText(String.valueOf(lowStockCount));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Data Loading Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleEShoppingOrders() {
        loadView("eshopping-orders.fxml");
    }
    
    @FXML
    protected void handleEShoppingProductsManager() {
        loadView("eshopping-products-manager.fxml");
    }
    
    @FXML
    protected void handleCustomerView() {
        loadView("section-selector.fxml");
    }
    
    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }
} 