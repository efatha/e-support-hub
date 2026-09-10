package com.efatha.e_support_hub_backend;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        String customer = ticket.getCustomer() == null
                ? ""
                : ticket.getCustomer().trim();

        String subject = ticket.getSubject() == null
                ? ""
                : ticket.getSubject().trim();

        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Customer is required");
        }

        if (subject.isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }

        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setInitials(generateInitials(customer));

        /*
         * Every new ticket receives its own permanent timestamp.
         */
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(Instant.now());
        }

        /*
         * Default status.
         */
        if (ticket.getStatus() == null || ticket.getStatus().isBlank()) {
            ticket.setStatus("Open");
        }

        /*
         * Default priority.
         */
        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            ticket.setPriority("Medium");
        }

        /*
         * Generate a new unique ticket ID.
         */
        if (ticket.getId() == null || ticket.getId().isBlank()) {
            ticket.setId(generateNextTicketId());
        }

        /*
         * Save this ticket as a NEW database record.
         */
        return ticketRepository.save(ticket);
    }

    private String generateNextTicketId() {

        long nextNumber = 1045;

        List<Ticket> existingTickets = ticketRepository.findAll();

        for (Ticket existingTicket : existingTickets) {

            String existingId = existingTicket.getId();

            if (existingId == null || existingId.isBlank()) {
                continue;
            }

            String numericPart = existingId.replace("#", "").trim();

            try {
                long currentNumber = Long.parseLong(numericPart);

                if (currentNumber >= nextNumber) {
                    nextNumber = currentNumber + 1;
                }

            } catch (NumberFormatException ignored) {
                // Ignore IDs that are not numeric.
            }
        }

        return "#" + nextNumber;
    }

    private void seedTickets() {

        /*
         * Create the sample tickets only when the database is empty.
         *
         * Once tickets already exist, do not recreate or replace them.
         */
        if (ticketRepository.count() > 0) {
            return;
        }

        ticketRepository.saveAll(List.of(

                new Ticket(
                        "#1048",
                        "Unable to reset password",
                        "Sarah Johnson",
                        "SJ",
                        "Open",
                        "High",
                        Instant.now().minus(12, ChronoUnit.MINUTES)
                ),

                new Ticket(
                        "#1047",
                        "Invoice download is not working",
                        "David Smith",
                        "DS",
                        "In Progress",
                        "Medium",
                        Instant.now().minus(45, ChronoUnit.MINUTES)
                ),

                new Ticket(
                        "#1046",
                        "Account successfully upgraded",
                        "Grace Williams",
                        "GW",
                        "Resolved",
                        "Low",
                        Instant.now().minus(90, ChronoUnit.MINUTES)
                ),

                new Ticket(
                        "#1045",
                        "Payment was charged twice",
                        "Michael Brown",
                        "MB",
                        "Open",
                        "Urgent",
                        Instant.now().minus(180, ChronoUnit.MINUTES)
                )
        ));
    }

    private String generateInitials(String customer) {

        if (customer == null || customer.trim().isEmpty()) {
            return "NA";
        }

        String[] parts = customer.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0]
                    .substring(0, 1)
                    .toUpperCase();
        }

        return (
                parts[0].substring(0, 1)
                        + parts[parts.length - 1].substring(0, 1)
        ).toUpperCase();
    }
}
