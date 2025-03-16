package com.jobplatform.repositories;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :userA AND m.receiver = :userB) OR (m.sender = :userB AND m.receiver = :userA) ORDER BY m.createdAt ASC")
    Page<ChatMessage> findChatHistory(@Param("userA") String userA, @Param("userB") String userB, Pageable pageable);

    List<ChatMessage> findDistinctBySenderOrReceiverOrderByCreatedAt(String sender, String receiver);

}
