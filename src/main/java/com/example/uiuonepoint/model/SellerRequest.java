package com.example.uiuonepoint.model;

import java.sql.Timestamp;

public class SellerRequest {
    private int id;
    private int userId;
    private String username; // For display purposes
    private String status; // "pending", "approved", "rejected"
    private Timestamp requestDate;
    private String reason; // Optional reason provided by the customer

    public SellerRequest() {}

    public SellerRequest(int id, int userId, String username, String status, Timestamp requestDate, String reason) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.requestDate = requestDate;
        this.reason = reason;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
} 