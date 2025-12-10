package com.resolveit.grievancemanagement.repository;

import com.resolveit.grievancemanagement.entity.ComplaintComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintCommentRepository extends JpaRepository<ComplaintComment, Long> {
    
    List<ComplaintComment> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
    
    List<ComplaintComment> findByComplaintIdAndIsInternalOrderByCreatedAtAsc(Long complaintId, Boolean isInternal);
}
