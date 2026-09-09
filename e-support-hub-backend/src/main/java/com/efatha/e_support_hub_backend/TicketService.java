package com.efatha.e_support_hub_backend;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        seedTickets();
    }

    public List<Ticket> getTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    public Ticket createTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }

        String customer = ticket.getCustomer() == null ? "" : ticket.getCustomer().trim();
        String subject = ticket.getSubject() == null ? "" : ticket.getSubject().trim();

        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (subject.isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }

        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setInitials(generateInitials(customer));

        // ✅ Store actual timestamp
        ticket.setCreatedAt(LocalDateTime.now());

        if (ticket.getStatus() == null || ticket.getStatus().isBlank()) {
            ticket.setStatus("Open");
        }
        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            ticket.setPriority("Medium");
        }
        if (ticket.getId() == null || ticket.getId().isBlank()) {
            ticket.setId("#" + (1000 + ticketRepository.count() + 1));
        }

        return ticketRepository.save(ticket);
    }

    private void seedTickets() {
        if (ticketRepository.count() > 0) {
            return;
        }

        ticketRepository.saveAll(List.of(
                new Ticket("#1048", "Unable to reset password", "Sarah Johnson", "SJ",
                        "Open", "High", LocalDateTime.now().minusMinutes(12)),
                new Ticket("#1047", "Invoice download is not working", "David Smith", "DS",
                        "In Progress", "Medium", LocalDateTime.now().minusMinutes(45)),
                new Ticket("#1046", "Account successfully upgraded", "Grace Williams", "GW",
                        "Resolved", "Low", LocalDateTime.now().minusHours(2)),
                new Ticket("#1045", "Payment was charged twice", "Michael Brown", "MB",
                        "Open", "Urgent", LocalDateTime.now().minusHours(3))
        ));
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
