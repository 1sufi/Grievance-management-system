package com.resolveit.grievancemanagement.service;

import com.resolveit.grievancemanagement.entity.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email notification to the citizen/anonymous user when the complaint status changes
     * to one of the tracked states (UNDER_REVIEW, IN_PROGRESS, RESOLVED, ESCALATED).
     */
    public void sendComplaintStatusChangeNotification(Complaint complaint,
                                                      Complaint.Status oldStatus,
                                                      String statusChangeComment) {
        if (complaint == null) {
            return;
        }

        Complaint.Status newStatus = complaint.getStatus();
        if (newStatus == null || newStatus == oldStatus) {
            return; // No change
        }

        if (!shouldNotifyOnStatus(newStatus)) {
            return; // Only notify for specific statuses
        }

        String recipientEmail = resolveRecipientEmail(complaint);
        if (recipientEmail == null || recipientEmail.isBlank()) {
            logger.warn("Skipping status-change email for complaint {} because recipient email is missing",
                    complaint.getId());
            return;
        }

        String ticketNumber = formatTicketNumber(complaint.getId());
        String subject = String.format("Update on your complaint %s: %s",
                ticketNumber,
                toReadableStatus(newStatus));

        String body = buildEmailBody(complaint, oldStatus, newStatus, statusChangeComment, ticketNumber);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Sent status-change email for complaint {} to {}", complaint.getId(), recipientEmail);
        } catch (Exception ex) {
            logger.error("Failed to send status-change email for complaint {}: {}",
                    complaint.getId(), ex.getMessage(), ex);
        }
    }

    private boolean shouldNotifyOnStatus(Complaint.Status status) {
        return status == Complaint.Status.UNDER_REVIEW
                || status == Complaint.Status.IN_PROGRESS
                || status == Complaint.Status.RESOLVED
                || status == Complaint.Status.ESCALATED;
    }

    private String resolveRecipientEmail(Complaint complaint) {
        // Named (non-anonymous) complaints go to the user's registered email
        if (complaint.getIsAnonymous() == null || !complaint.getIsAnonymous()) {
            return complaint.getUser() != null ? complaint.getUser().getEmail() : null;
        }
        // Anonymous complaints go to the anonymous email if it exists
        return complaint.getAnonymousEmail();
    }

    private String formatTicketNumber(Long id) {
        if (id == null) {
            return "(unassigned)";
        }
        return "CMP-" + String.format("%06d", id);
    }

    private String toReadableStatus(Complaint.Status status) {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case UNDER_REVIEW -> "Under Review";
            case IN_PROGRESS -> "In Progress";
            case RESOLVED -> "Resolved";
            case ESCALATED -> "Escalated";
            case NEW -> "New";
            case CLOSED -> "Closed";
        };
    }

    private String buildEmailBody(Complaint complaint,
                                  Complaint.Status oldStatus,
                                  Complaint.Status newStatus,
                                  String statusChangeComment,
                                  String ticketNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear Citizen,\n\n");
        sb.append("This is to inform you that the status of your complaint has been updated.\n\n");

        sb.append("Complaint Ticket: ").append(ticketNumber).append("\n");
        sb.append("Title: ").append(complaint.getTitle() != null ? complaint.getTitle() : "N/A").append("\n");

        if (oldStatus != null) {
            sb.append("Previous Status: ").append(toReadableStatus(oldStatus)).append("\n");
        }
        sb.append("Current Status: ").append(toReadableStatus(newStatus)).append("\n\n");

        if (statusChangeComment != null && !statusChangeComment.isBlank()) {
            sb.append("Message from the administration/officer:\n");
            sb.append(statusChangeComment).append("\n\n");
        }

        sb.append("You can log in to the portal to view more details about your complaint.\n\n");
        sb.append("Regards,\n");
        sb.append("ResolveIT Grievance Management System");

        return sb.toString();
    }
}
