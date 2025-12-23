package com.numlock.pika.service;

import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.user.AdminUserDto;
import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport; // StreamSupport 임포트 추가

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDto> getAllUsersForAdmin() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(AdminUserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDto getUserByIdForAdmin(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return AdminUserDto.fromEntity(user);
    }

    @Override
    @Transactional
    public void warnUser(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        // user.setWarningCount(user.getWarningCount() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restrictUser(String userId, String reason, LocalDateTime endDate) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        // user.setIsRestricted(true);
        // user.setRestrictionReason(reason);
        // user.setRestrictionEndDate(endDate);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void liftRestriction(String userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        // user.setIsRestricted(false);
        // user.setRestrictionReason(null);
        // user.setRestrictionEndDate(null);
        userRepository.save(user);
    }
}
