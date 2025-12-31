package com.numlock.pika.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.numlock.pika.domain.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 특정 두 사용자 간의 메시지 기록을 가져옴 (대화 내역)
    @Query("SELECT m FROM Message m WHERE (m.senderId = :user1 AND m.recipientId = :user2) OR (m.senderId = :user2 AND m.recipientId = :user1) ORDER BY m.sentAt ASC")
    List<Message> findConversationBetweenUsers(@Param("user1") String user1Id, @Param("user2") String user2Id);

    // 상품 ID를 기반으로 특정 두 사용자 간의 메시지 기록을 가져옴
    @Query("SELECT m FROM Message m WHERE m.product.productId = :productId AND ((m.senderId = :user1 AND m.recipientId = :user2) OR (m.senderId = :user2 AND m.recipientId = :user1)) ORDER BY m.sentAt ASC")
    List<Message> findConversationByProduct(@Param("user1") String user1Id, @Param("user2") String user2Id, @Param("productId") Integer productId);

    @Query("SELECT m FROM Message m WHERE m.id IN (SELECT MAX(m2.id) FROM Message m2 WHERE (m2.senderId = :userId OR m2.recipientId = :userId) AND m2.product IS NOT NULL GROUP BY m2.product.productId, (CASE WHEN m2.senderId = :userId THEN m2.recipientId ELSE m2.senderId END)) ORDER BY m.sentAt DESC")
    List<Message> findLatestMessagesPerChatRoom(@Param("userId") String userId);

    long countByProductProductIdAndRecipientIdAndSenderIdAndIsRead(Integer productId, String recipientId, String senderId, boolean isRead);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.isRead = true WHERE m.product.productId = :productId AND ((m.senderId = :user1 AND m.recipientId = :user2) OR (m.senderId = :user2 AND m.recipientId = :user1)) "+
           "AND m.isRead = false")
    void markAsReadByRoomIdAndUserId(@Param("user1") String user1Id, @Param("user2") String user2Id, @Param("productId") Integer productId);
    // 특정 사용자에게 온 메시지 중 읽지 않은 메시지 수
    long countByRecipientIdAndIsReadFalse(String recipientId);

    // 해당 상품에 대한 채팅 메시지가 존재하는지 확인
    boolean existsByProduct_ProductId(int productId);
}
