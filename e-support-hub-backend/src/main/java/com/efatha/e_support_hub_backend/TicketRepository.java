package com.efatha.e_support_hub_backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    List<Ticket> findAllByOrderByIdDesc();
}