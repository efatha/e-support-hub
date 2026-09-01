package com.efatha.e_support_hub_backend;

public class Ticket {
    private String id;
    private String subject;
    private String customer;
    private String initials;
    private String status;
    private String priority;
    private String time;

    public Ticket() {
    }

    public Ticket(String id, String subject, String customer, String initials, String status, String priority, String time) {
        this.id = id;
        this.subject = subject;
        this.customer = customer;
        this.initials = initials;
        this.status = status;
        this.priority = priority;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
