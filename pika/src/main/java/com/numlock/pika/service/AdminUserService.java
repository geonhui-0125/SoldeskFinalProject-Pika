package com.numlock.pika.service;

import com.numlock.pika.dto.user.AdminUserDto; // AdminUserDto 사용

import java.time.LocalDateTime;
import java.util.List;

public interface AdminUserService {

    /**
     * 관리자 페이지에 표시할 모든 사용자 목록을 가져옵니다.
     * @return AdminUserDto 목록
     */
    List<AdminUserDto> getAllUsersForAdmin();

    /**
     * 특정 사용자 ID를 가진 사용자의 상세 정보를 관리자용으로 가져옵니다.
     * @param userId 사용자 ID
     * @return AdminUserDto
     */
    AdminUserDto getUserByIdForAdmin(String userId);

    /**
     * 특정 사용자에게 경고를 부여하고 경고 횟수를 증가시킵니다.
     * @param userId 경고를 부여할 사용자 ID
     */
    void warnUser(String userId);

    /**
     * 특정 사용자를 제한합니다.
     * @param userId 제한할 사용자 ID
     * @param reason 제한 사유
     * @param endDate 제한 종료일 (임시 제한의 경우)
     */
    void restrictUser(String userId, String reason, LocalDateTime endDate);

    /**
     * 특정 사용자의 제한을 해제합니다.
     * @param userId 제한을 해제할 사용자 ID
     */
    void liftRestriction(String userId);
}
