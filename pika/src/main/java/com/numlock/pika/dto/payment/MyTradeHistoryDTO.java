package com.numlock.pika.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyTradeHistoryDTO {
    private List<SalesResponseDTO> sales;     // 판매 내역 리스트
    private List<PurchaseResponseDTO> purchases; // 구매 내역 리스트
}
