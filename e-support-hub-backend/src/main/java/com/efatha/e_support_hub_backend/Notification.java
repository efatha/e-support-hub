package com.efatha.e_support_hub_backend;

import java.time.Instant;

public class Notification {

    private final String id;
    private final String ticketId;
    private final String customer;
    private final String message;
    private final Instant createdAt;
    private boolean read;

    public Notification(String id, String ticketId, String customer, String message, Instant createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.customer = customer;
        this.message = message;
        this.createdAt = createdAt;
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getCustomer() {
        return customer;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        this.read = true;
    }
}
