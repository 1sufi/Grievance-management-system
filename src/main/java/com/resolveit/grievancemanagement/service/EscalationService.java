package com.resolveit.grievancemanagement.service;

import com.resolveit.grievancemanagement.entity.Complaint;
import com.resolveit.grievancemanagement.entity.ComplaintStatusHistory;
import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.ComplaintRepository;
import com.resolveit.grievancemanagement.repository.ComplaintStatusHistoryRepository;
import com.resolveit.grievancemanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class EscalationService {

    private static final Logger logger = LoggerFactory.getLogger(EscalationService.class);
    
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;
    
    // Default escalation threshold: 24 hours (can be configured per complaint)
    private static final int DEFAULT_ESCALATION_HOURS = 24;
    
    public EscalationService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            ComplaintStatusHistoryRepository statusHistoryRepository,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Scheduled task that runs every hour to check for complaints that need escalation.
     *
     * Rules:
     *  - Unassigned complaints that sit too long are auto-escalated to an L2 officer.
     *  - Assigned L1 complaints with a passed due date (deadline) and not resolved are
     *    also auto-escalated to an L2 officer.
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 milliseconds)
    @Transactional
    public void runEscalationChecks() {
        logger.info("Starting escalation checks (unassigned + overdue assigned complaints)");

        LocalDateTime now = LocalDateTime.now();

        // Get all L2 officers (shared for both flows)
        List<User> l2Officers = userRepository.findByRoleAndOfficerLevel(
                User.Role.OFFICER, User.OfficerLevel.L2);

        if (l2Officers.isEmpty()) {
            logger.warn("No L2 officers found. Cannot escalate complaints.");
            return;
        }

        Random random = new Random();
        int escalatedCount = 0;

        // 1) Unassigned complaints based on createdAt + threshold hours
        List<Complaint> unassignedComplaints = complaintRepository.findUnassignedComplaintsForEscalation(
                List.of(Complaint.Status.NEW, Complaint.Status.UNDER_REVIEW));

        logger.info("Found {} unassigned complaints to check for escalation", unassignedComplaints.size());

        for (Complaint complaint : unassignedComplaints) {
            try {
                int thresholdHours = complaint.getEscalationThresholdHours() != null
                        ? complaint.getEscalationThresholdHours()
                        : DEFAULT_ESCALATION_HOURS;

                LocalDateTime escalationTime = complaint.getCreatedAt().plusHours(thresholdHours);

                if (now.isAfter(escalationTime) || now.isEqual(escalationTime)) {
                    User assignedL2Officer = l2Officers.get(random.nextInt(l2Officers.size()));

                    Complaint.Status oldStatus = complaint.getStatus();
                    complaint.setAssignedOfficer(assignedL2Officer);
                    complaint.setStatus(Complaint.Status.ESCALATED);

                    complaintRepository.save(complaint);

                    String comment = String.format(
                            "Complaint auto-escalated to L2 officer %s after %d hours without assignment",
                            assignedL2Officer.getUsername(), thresholdHours);

                    ComplaintStatusHistory history = new ComplaintStatusHistory(
                            Complaint.Status.ESCALATED,
                            comment,
                            complaint,
                            null
                    );
                    statusHistoryRepository.save(history);

                    notificationService.sendComplaintStatusChangeNotification(complaint, oldStatus, comment);

                    escalatedCount++;
                    logger.info("Escalated unassigned complaint ID {} to L2 officer {}",
                            complaint.getId(), assignedL2Officer.getUsername());
                }
            } catch (Exception e) {
                logger.error("Error escalating unassigned complaint ID {}: {}", complaint.getId(), e.getMessage(), e);
            }
        }

        // 2) Assigned L1 complaints where dueDate has passed and status is still active
        List<Complaint.Status> activeStatuses = List.of(
                Complaint.Status.NEW,
                Complaint.Status.UNDER_REVIEW,
                Complaint.Status.IN_PROGRESS
        );

        List<Complaint> overdueAssignedComplaints = complaintRepository.findOverdueComplaintsForEscalation(
                User.OfficerLevel.L1, now, activeStatuses);

        logger.info("Found {} assigned L1 complaints with passed deadline to check for escalation",
                overdueAssignedComplaints.size());

        for (Complaint complaint : overdueAssignedComplaints) {
            try {
                User assignedL2Officer = l2Officers.get(random.nextInt(l2Officers.size()));

                Complaint.Status oldStatus = complaint.getStatus();
                complaint.setAssignedOfficer(assignedL2Officer);
                complaint.setStatus(Complaint.Status.ESCALATED);

                complaintRepository.save(complaint);

                String comment = String.format(
                        "Complaint auto-escalated to L2 officer %s after missing deadline %s",
                        assignedL2Officer.getUsername(),
                        complaint.getDueDate() != null ? complaint.getDueDate().toString() : "N/A");

                ComplaintStatusHistory history = new ComplaintStatusHistory(
                        Complaint.Status.ESCALATED,
                        comment,
                        complaint,
                        null
                );
                statusHistoryRepository.save(history);

                notificationService.sendComplaintStatusChangeNotification(complaint, oldStatus, comment);

                escalatedCount++;
                logger.info("Escalated overdue complaint ID {} to L2 officer {}",
                        complaint.getId(), assignedL2Officer.getUsername());
            } catch (Exception e) {
                logger.error("Error escalating overdue complaint ID {}: {}", complaint.getId(), e.getMessage(), e);
            }
        }

        logger.info("Escalation checks completed. Escalated {} complaints in total", escalatedCount);
    }
    
    /**
     * Manual escalation method that can be called from admin services
     */
    @Transactional
    public void escalateComplaint(Long complaintId, Long l2OfficerId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        
        User l2Officer = userRepository.findById(l2OfficerId)
                .orElseThrow(() -> new IllegalArgumentException("L2 officer not found"));
        
        if (l2Officer.getRole() != User.Role.OFFICER || 
            l2Officer.getOfficerLevel() != User.OfficerLevel.L2) {
            throw new IllegalArgumentException("User must be an L2 officer");
        }
        
        Complaint.Status oldStatus = complaint.getStatus();
        complaint.setAssignedOfficer(l2Officer);
        complaint.setStatus(Complaint.Status.ESCALATED);
        
        complaintRepository.save(complaint);
        
        String comment = "Complaint manually escalated to L2 officer";

        ComplaintStatusHistory history = new ComplaintStatusHistory(
                Complaint.Status.ESCALATED,
                comment,
                complaint,
                null
        );
        statusHistoryRepository.save(history);

        notificationService.sendComplaintStatusChangeNotification(complaint, oldStatus, comment);
        
        logger.info("Manually escalated complaint ID {} to L2 officer {}", 
                complaint.getId(), l2Officer.getUsername());
    }
}

