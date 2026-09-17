package com.efatha.e_support_hub_backend;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;
    private final NotificationService notificationService; // ✅ add this

    public TicketController(TicketService ticketService, NotificationService notificationService) {
        this.ticketService = ticketService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Welcome to E-Support Hub Backend!");
        response.put("tickets", ticketService.getTickets());
        return response;
    }

    @GetMapping("/tickets")
    public List<Ticket> getTickets() {
        return ticketService.getTickets();
    }

    @PostMapping("/tickets")
    public Ticket createTicket(@RequestBody Ticket ticket) {
        Ticket savedTicket = ticketService.createTicket(ticket);
        return savedTicket;
    }

    @GetMapping("/notifications")
    public Map<String, Object> getNotifications() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", notificationService.getUnreadCount());
        response.put("notifications", notificationService.getNotifications());
        return response;
    }

    @PostMapping("/notifications/read")
    public Map<String, Object> markNotificationsRead() {
        notificationService.markAllAsRead();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", notificationService.getUnreadCount());
        response.put("notifications", notificationService.getNotifications());
        return response;
    }

    @DeleteMapping("/notifications")
    public Map<String, Object> clearNotifications() {
        notificationService.clearNotifications();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", notificationService.getUnreadCount());
        response.put("notifications", notificationService.getNotifications());
        return response;
    }
}
