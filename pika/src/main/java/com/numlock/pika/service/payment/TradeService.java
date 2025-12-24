package com.numlock.pika.service.payment;

import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.payment.MyTradeHistoryDTO;
import com.numlock.pika.dto.payment.PurchaseResponseDTO;
import com.numlock.pika.dto.payment.SalesResponseDTO;
import com.numlock.pika.repository.PaymentRepository;
import com.numlock.pika.service.product.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final PaymentRepository paymentRepository;
    private final ProductServiceImpl proImpl;

    public MyTradeHistoryDTO getMyTradeHistory(Users user) {
        // 판매 내역 가져오기 및 변환
        List<SalesResponseDTO> sales = paymentRepository.findBySellerOrderByCreatedAtDesc(user)
                .stream().map(p -> SalesResponseDTO.builder()
                        .impUid(p.getImpUid())
                        .amount(p.getAmount())
                        .soldAt(p.getCreatedAt())
                        .productId(p.getTask().getProductId())
                        .title(p.getTask().getTitle())
                        .productImage(proImpl.getImageUrls(p.getTask().getProductImage()).get(0))
                        .productState(p.getTask().getProductState())
                        .statusText(convertState(p.getTask().getProductState()))
                        .buyerNickname(p.getBuyer().getNickname())
                        .buyerId(p.getBuyer().getId())
                        .build()
                ).toList();

        // 구매 내역 가져오기 및 변환
        List<PurchaseResponseDTO> purchases = paymentRepository.findByBuyerOrderByCreatedAtDesc(user)
                .stream().map(p -> PurchaseResponseDTO.builder()
                        .impUid(p.getImpUid())
                        .amount(p.getAmount())
                        .purchasedAt(p.getCreatedAt())
                        .productId(p.getTask().getProductId())
                        .title(p.getTask().getTitle())
                        .productImage(proImpl.getImageUrls(p.getTask().getProductImage()).get(0))
                        .productState(p.getTask().getProductState())
                        .statusText(convertState(p.getTask().getProductState()))
                        .sellerNickname(p.getSeller().getNickname())
                        .sellerId(p.getSeller().getId())
                        .build()
                ).toList();

        return MyTradeHistoryDTO.builder()
                .sales(sales)
                .purchases(purchases)
                .build();
    }

    private String convertState(int state) {
        return switch (state) {
            case 0 -> "판매중";
            case 1 -> "판매완료";
            case 2 -> "거래중(확정대기)";
            case 3 -> "승인대기"; // 판매자와 구매자 모두에게 동일하게 표시
            default -> "상태미정";
        };
    }
}