package com.efatha.e_support_hub_backend;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
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
        return ticketService.createTicket(ticket);
    }
}
