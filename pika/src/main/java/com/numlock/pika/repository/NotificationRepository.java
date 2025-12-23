package com.numlock.pika.repository;

import com.numlock.pika.domain.Notifications;
import com.numlock.pika.domain.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    // 특정 유저의 알림 목록을 최신순으로 조회
    Page<Notifications> findByReceiverAndIsReadOrderByCreatedAtDesc(Users receiver, Integer isRead, Pageable pageable);

    // 안읽은 알림 개수 조회
    long countByReceiverAndIsRead(Users receiver, Integer isRead);

    List<Notifications> findByReceiverOrderByCreatedAtDesc(Users receiver);

    void deleteByNotificationId(Long notificationId);

    void deleteByReceiver(Users receiver);

    // 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notifications n SET n.isRead = 1 WHERE n.notificationId = :id")
    void markAsRead(@Param("id") Long id);
}