package com.resolveit.grievancemanagement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.resolveit.grievancemanagement.entity.Complaint;
import com.resolveit.grievancemanagement.entity.User;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    List<Complaint> findByUser(User user);
    
    Page<Complaint> findByUser(User user, Pageable pageable);
    
    List<Complaint> findByAssignedOfficer(User assignedOfficer);
    
    Page<Complaint> findByAssignedOfficer(User assignedOfficer, Pageable pageable);
    
    List<Complaint> findByStatus(Complaint.Status status);
    
    List<Complaint> findByCategory(Complaint.Category category);
    
    List<Complaint> findByPriority(Complaint.Priority priority);
    
    List<Complaint> findByIsAnonymous(Boolean isAnonymous);
    
    @Query("SELECT c FROM Complaint c WHERE c.status = :status AND c.createdAt < :threshold")
    List<Complaint> findComplaintsForEscalation(@Param("status") Complaint.Status status, 
                                               @Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT c FROM Complaint c WHERE c.assignedOfficer = :officer AND c.status IN :statuses")
    List<Complaint> findByAssignedOfficerAndStatusIn(@Param("officer") User officer, 
                                                    @Param("statuses") List<Complaint.Status> statuses);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    Long countByStatus(@Param("status") Complaint.Status status);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.assignedOfficer = :officer AND c.status = :status")
    Long countByAssignedOfficerAndStatus(@Param("officer") User officer, 
                                        @Param("status") Complaint.Status status);
    
    @Query("SELECT DISTINCT c FROM Complaint c " +
           "LEFT JOIN FETCH c.assignedOfficer " +
           "LEFT JOIN FETCH c.statusHistory sh " +
           "LEFT JOIN FETCH sh.changedBy " +
           "WHERE c.id = :id")
    java.util.Optional<Complaint> findByIdWithStatusHistory(@Param("id") Long id);
    
    @Query("SELECT DISTINCT c FROM Complaint c " +
           "LEFT JOIN FETCH c.assignedOfficer " +
           "WHERE c.user = :user")
    List<Complaint> findByUserWithOfficer(@Param("user") User user);
    
    @Query("SELECT c FROM Complaint c WHERE c.assignedOfficer IS NULL " +
           "AND c.status IN :statuses")
    List<Complaint> findUnassignedComplaintsForEscalation(
            @Param("statuses") List<Complaint.Status> statuses);

    @Query("SELECT c FROM Complaint c " +
           "WHERE c.assignedOfficer IS NOT NULL " +
           "AND c.assignedOfficer.officerLevel = :level " +
           "AND c.dueDate IS NOT NULL " +
           "AND c.dueDate <= :now " +
           "AND c.status IN :statuses")
    List<Complaint> findOverdueComplaintsForEscalation(
            @Param("level") com.resolveit.grievancemanagement.entity.User.OfficerLevel level,
            @Param("now") java.time.LocalDateTime now,
            @Param("statuses") List<Complaint.Status> statuses);
}
