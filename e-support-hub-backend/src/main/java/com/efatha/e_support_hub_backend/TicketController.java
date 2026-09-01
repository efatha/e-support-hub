package com.efatha.e_support_hub_backend;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final List<Ticket> tickets = new ArrayList<>();

    public TicketController() {
        tickets.add(new Ticket("#1048", "Unable to reset password", "Sarah Johnson", "SJ", "Open", "High", "12 min ago"));
        tickets.add(new Ticket("#1047", "Invoice download is not working", "David Smith", "DS", "In Progress", "Medium", "45 min ago"));
        tickets.add(new Ticket("#1046", "Account successfully upgraded", "Grace Williams", "GW", "Resolved", "Low", "2 hours ago"));
        tickets.add(new Ticket("#1045", "Payment was charged twice", "Michael Brown", "MB", "Open", "Urgent", "3 hours ago"));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Welcome to E-Support Hub Backend!");
        response.put("tickets", tickets);
        return response;
    }

    @GetMapping("/tickets")
    public List<Ticket> getTickets() {
        return tickets;
    }

    @PostMapping("/tickets")
    public Ticket createTicket(@RequestBody Ticket ticket) {
        String initials = generateInitials(ticket.getCustomer());
        ticket.setInitials(initials);
        ticket.setTime("just now");

        if (ticket.getStatus() == null || ticket.getStatus().isBlank()) {
            ticket.setStatus("Open");
        }

        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            ticket.setPriority("Medium");
        }

        if (ticket.getId() == null || ticket.getId().isBlank()) {
            String nextId = "#" + (1000 + tickets.size() + 1);
            ticket.setId(nextId);
        }

        tickets.add(0, ticket);
        return ticket;
    }

    private String generateInitials(String customer) {
        if (customer == null || customer.trim().isEmpty()) {
            return "NA";
        }

        String[] parts = customer.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
