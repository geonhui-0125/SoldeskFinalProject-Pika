package com.numlock.pika.service.payment;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Payments;
import com.numlock.pika.domain.Products;
import com.numlock.pika.dto.PaymentDto;
import com.numlock.pika.repository.AccountRepository;
import com.numlock.pika.repository.PaymentRepository;
import com.numlock.pika.repository.ProductRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private ProductRepository productRepository;

    //생성자로 IamportClient 초기화
    //불변성 보장, null값 방지
    //properties.application 파일의 포트원 키 정보 가져옴

    private final String impCode;

    public PaymentService(@Value("${imp.code}") String impCode) {

        this.impCode = impCode;
    }

    public PaymentDto getPayemntInfo(int productId) {
        Optional<Products> productOptional = productRepository.findById(productId);
        Products product = productOptional.orElseThrow(() -> new RuntimeException("해당 상품을 찾지 못했습니다. "));

        //데이터 베이스에서 조회해온 상품의 정보
        //결제DTO에 전달
        PaymentDto paymentDto;

        paymentDto = PaymentDto.builder()
                .pg("html5_inicis") // 포트원에 연동해놓은 pg사 String 전달
                .py_method("card") // 결제 수단은 card로 고정
                .merchantUid(product.getSellerId()+"_"+ LocalDateTime.now())
                .name(product.getTitle()) // 상품 제목 전달
                .impUid(impCode) // 고유 결제
                .taskId(product.getProductId()) //상품 고유 id 전달
                .amount(product.getPrice())
                .build();

        return paymentDto;
    }



}
