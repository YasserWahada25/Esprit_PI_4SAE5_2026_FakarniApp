package com.alzheimer.group_service.repositories;

import com.alzheimer.group_service.entities.GroupMember;
import com.alzheimer.group_service.entities.MemberRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends CrudRepository<GroupMember, Long> {
    
    List<GroupMember> findByGroupId(Long groupId);
    
    List<GroupMember> findByUserId(String userId);
    
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, String userId);
    
    boolean existsByGroupIdAndUserId(Long groupId, String userId);
    
    List<GroupMember> findByGroupIdAndRole(Long groupId, MemberRole role);
    
    long countByGroupId(Long groupId);
    
    void deleteByGroupIdAndUserId(Long groupId, String userId);
}
