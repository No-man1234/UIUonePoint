package com.example.uiuonepoint.util;

import com.example.uiuonepoint.dao.FoodOrderDAO;
import com.example.uiuonepoint.dao.GroceryOrderDAO;
import com.example.uiuonepoint.model.FoodOrder;
import com.example.uiuonepoint.model.GroceryOrder;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderSchedulerService {
    private static final Logger LOGGER = Logger.getLogger(OrderSchedulerService.class.getName());
    private static final int ORDER_TIMEOUT_MINUTES = 30;
    
    private final ScheduledExecutorService scheduler;
    private final FoodOrderDAO foodOrderDAO;
    private final GroceryOrderDAO groceryOrderDAO;
    
    private static OrderSchedulerService instance;
    
    private OrderSchedulerService() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.foodOrderDAO = new FoodOrderDAO();
        this.groceryOrderDAO = new GroceryOrderDAO();
    }
    
    public static synchronized OrderSchedulerService getInstance() {
        if (instance == null) {
            instance = new OrderSchedulerService();
        }
        return instance;
    }
    
    public void start() {
        // Check every minute for orders that need to be canceled
        scheduler.scheduleAtFixedRate(this::checkPendingOrders, 0, 1, TimeUnit.MINUTES);
        LOGGER.info("Order scheduler service started - will cancel orders after " + ORDER_TIMEOUT_MINUTES + " minutes");
    }
    
    public void stop() {
        scheduler.shutdown();
        LOGGER.info("Order scheduler service stopped");
    }
    
    private void checkPendingOrders() {
        try {
            // Check food orders
            checkPendingFoodOrders();
            
            // Check grocery orders
            checkPendingGroceryOrders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking pending orders", e);
        }
    }
    
    private void checkPendingFoodOrders() {
        try {
            List<FoodOrder> pendingOrders = foodOrderDAO.getPendingOrdersForCancellation();
            
            for (FoodOrder order : pendingOrders) {
                Timestamp orderTime = order.getOrderTime();
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                
                // Calculate difference in milliseconds
                long diffInMilliseconds = currentTime.getTime() - orderTime.getTime();
                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMilliseconds);
                
                if (diffInMinutes >= ORDER_TIMEOUT_MINUTES) {
                    // Cancel the order
                    order.setStatus("cancelled");
                    foodOrderDAO.update(order);
                    LOGGER.info("Food order #" + order.getId() + " automatically cancelled after " + diffInMinutes + " minutes");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking pending food orders", e);
        }
    }
    
    private void checkPendingGroceryOrders() {
        try {
            List<GroceryOrder> pendingOrders = groceryOrderDAO.getPendingOrdersForCancellation();
            
            for (GroceryOrder order : pendingOrders) {
                Timestamp orderTime = order.getOrderTime();
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                
                // Calculate difference in milliseconds
                long diffInMilliseconds = currentTime.getTime() - orderTime.getTime();
                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMilliseconds);
                
                if (diffInMinutes >= ORDER_TIMEOUT_MINUTES) {
                    // Cancel the order
                    order.setStatus("cancelled");
                    groceryOrderDAO.update(order);
                    LOGGER.info("Grocery order #" + order.getId() + " automatically cancelled after " + diffInMinutes + " minutes");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking pending grocery orders", e);
        }
    }
} 