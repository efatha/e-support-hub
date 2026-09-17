package com.efatha.e_support_hub_backend;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NotificationService {

    private final List<Notification> notifications = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public void notifyTicketCreation(Ticket ticket) {
        String customer = ticket.getCustomer() == null || ticket.getCustomer().isBlank()
                ? "there"
                : ticket.getCustomer();

        String message = String.format(
                "Thanks %s for creating this ticket, the admin is reviewing this ticket %s for resolving this case.",
                customer,
                ticket.getId()
        );

        notifications.add(new Notification(
                String.valueOf(nextId.getAndIncrement()),
                ticket.getId(),
                customer,
                message,
                Instant.now()
        ));
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public long getUnreadCount() {
        return notifications.stream().filter(notification -> !notification.isRead()).count();
    }

    public void markAllAsRead() {
        notifications.forEach(Notification::markRead);
    }

    public void clearNotifications() {
        notifications.clear();
    }
}
