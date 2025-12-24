package com.numlock.pika.dto;

import com.numlock.pika.domain.Users;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProductDetailDto {

    private int productId;
    private String sellerId;
    private Users seller;
    private String buyerId;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String categoryOne;
    private String categoryTwo;
    private int productStat;
    private String buyerType;
    private int favoriteCnt;
    private boolean wished;
    private int viewCnt;
    private double star;
    private List<String> images;
    private String timeAgo;
    private String impUid;
    private boolean hasReviewed;

    public static String calculateTimeAgo(LocalDateTime createdAt) {

        if (createdAt == null) return "";

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + "초 전";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = hours / 24;
        if (days < 7) {
            return days + "일 전";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + "주 전";
        }

        long months = days / 30;
        if (months < 12) {
            return months + "개월 전";
        }

        long years = days / 365;
        return years + "년 전";
    }

}
