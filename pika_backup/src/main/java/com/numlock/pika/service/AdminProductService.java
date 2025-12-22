package com.numlock.pika.service;

import com.numlock.pika.dto.ProductDto; // 기존 ProductDto 사용 가정

import java.util.List;

public interface AdminProductService {

    /**
     * 관리자 페이지에 표시할 모든 판매글 목록을 가져옵니다.
     * @return ProductDto 목록
     */
    List<ProductDto> getAllProductsForAdmin();

    /**
     * 운영 정책 위반으로 판매글을 삭제(소프트 삭제)합니다.
     * @param productId 삭제할 판매글 ID
     * @param reason 삭제 사유
     */
    void deleteProductByAdmin(int productId, String reason);

    /**
     * 판매글을 운영 정책 위반으로 표시합니다. (예: 신고됨, 일시 정지)
     * @param productId 위반으로 표시할 판매글 ID
     * @param reason 위반 사유
     */
    void markProductAsViolatingPolicy(int productId, String reason);

    /**
     * 판매글의 운영 정책 위반 상태를 해제하고 정상으로 되돌립니다.
     * @param productId 상태를 해제할 판매글 ID
     */
    void liftProductViolation(int productId);
}
