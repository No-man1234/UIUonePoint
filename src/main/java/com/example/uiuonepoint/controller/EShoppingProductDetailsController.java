package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.EShoppingOrderDAO;
import com.example.uiuonepoint.dao.EShoppingProductDAO;
import com.example.uiuonepoint.dao.UserDAO;
import com.example.uiuonepoint.model.EShoppingOrder;
import com.example.uiuonepoint.model.EShoppingProduct;
import com.example.uiuonepoint.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class EShoppingProductDetailsController extends CustomerBaseController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private ImageView productImage;
    @FXML private Text productName;
    @FXML private Text productPrice;
    @FXML private Text productDescription;
    @FXML private Text productSeller;
    @FXML private Text productStock;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TextField mobileNumberField;

    private final EShoppingProductDAO productDAO = new EShoppingProductDAO();
    private final EShoppingOrderDAO orderDAO = new EShoppingOrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private EShoppingProduct product;
    private boolean initialized = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupQuantitySpinner();
        initialized = true;
    }

    private boolean isInitialized() {
        return initialized;
    }

    private void setupQuantitySpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);
    }

    @Override
    protected void onInitialize() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername());
            loadProductDetails();
        }
    }

    private void loadProductDetails() {
        if (product == null) return;

        productName.setText(product.getName());
        productPrice.setText(String.format("৳%.2f", product.getPrice()));
        productDescription.setText(product.getDescription());
        productStock.setText("Available Stock: " + product.getStockQuantity());

        try {
            Optional<User> seller = userDAO.get(product.getSellerId());
            seller.ifPresent(user -> productSeller.setText("Seller: " + user.getUsername()));
        } catch (SQLException e) {
            productSeller.setText("Seller: Unknown");
        }

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Image image = new Image(product.getImagePath());
                productImage.setImage(image);
            } catch (Exception e) {
                // Handle image loading error
            }
        }

        // Set max quantity based on available stock
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
            (SpinnerValueFactory.IntegerSpinnerValueFactory) quantitySpinner.getValueFactory();
        valueFactory.setMax(product.getStockQuantity());
    }

    public void setProduct(EShoppingProduct product) {
        this.product = product;
        if (isInitialized()) {
            loadProductDetails();
        }
    }

    @FXML
    protected void handlePlaceOrder() {
        if (product == null) {
            showError("Error", "Product information is not available");
            return;
        }

        String mobileNumber = mobileNumberField.getText().trim();
        if (mobileNumber.isEmpty()) {
            showError("Error", "Please enter your mobile number");
            return;
        }

        int quantity = quantitySpinner.getValue();
        if (quantity <= 0) {
            showError("Error", "Please select a valid quantity");
            return;
        }

        if (quantity > product.getStockQuantity()) {
            showError("Error", "Requested quantity exceeds available stock");
            return;
        }

        try {
            EShoppingOrder order = new EShoppingOrder();
            order.setUserId(currentUser.getId());
            order.setProductId(product.getId());
            order.setQuantity(quantity);
            order.setMobileNumber(mobileNumber);
            order.setStatus("pending");
            
            orderDAO.save(order);
            
            showInformation("Success", "Order placed successfully!");
            handleBack();
        } catch (SQLException e) {
            showError("Error", "Failed to place order: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBack() {
        loadView("eshopping-view.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/" + fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            Stage stage = (Stage) productImage.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            Object controller = fxmlLoader.getController();
            if (controller instanceof CustomerBaseController) {
                ((CustomerBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }
} 