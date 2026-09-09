package com.efatha.e_support_hub_backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    private String id;
    private String subject;
    private String customer;
    private String initials;
    private String status;
    private String priority;

    // Proper timestamp field
    private LocalDateTime createdAt;

    public Ticket() {}

    public Ticket(String id, String subject, String customer, String initials,
                  String status, String priority, LocalDateTime createdAt) {
        this.id = id;
        this.subject = subject;
        this.customer = customer;
        this.initials = initials;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
