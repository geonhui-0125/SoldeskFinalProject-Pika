package com.numlock.pika.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.numlock.pika.domain.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 특정 두 사용자 간의 메시지 기록을 가져옴 (대화 내역)
    @Query("SELECT m FROM Message m WHERE (m.senderId = :user1 AND m.recipientId = :user2) OR (m.senderId = :user2 AND m.recipientId = :user1) ORDER BY m.sentAt ASC")
    List<Message> findConversationBetweenUsers(@Param("user1") String user1Id, @Param("user2") String user2Id);

    // 특정 사용자에게 온 메시지 중 읽지 않은 메시지 수
    long countByRecipientIdAndIsReadFalse(String recipientId);
}
