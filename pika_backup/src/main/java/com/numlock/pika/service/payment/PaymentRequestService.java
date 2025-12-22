package com.numlock.pika.service.payment;

import com.numlock.pika.domain.Payments;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.payment.PaymentRequestDto;
import com.numlock.pika.repository.PaymentRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentRequestService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    //생성자로 IamportClient 초기화
    //불변성 보장, null값 방지
    //properties.application 파일의 포트원 키 정보 가져옴
    private final String impCode;

    public PaymentRequestService(@Value("${imp.code}") String impCode) {
        this.impCode = impCode;
    }

    public PaymentRequestDto getPaymentPreview(int productId, Principal principal) {

        // 구매하려는 상품의 Entity
        Optional<Products> productOptional = productRepository.findById(productId);
        Products products = productOptional.orElseThrow(() -> new RuntimeException("해당 상품을 찾지 못했습니다."));

        // 구매하려는 사용자 Entity
        Optional<Users> usersOptional = userRepository.findById(principal.getName());
        Users users = usersOptional.orElseThrow(() -> new RuntimeException("해당 사용자를 찾지 못했습니다."));

        //데이터 베이스에서 조회해온 상품의 정보
        //결제DTO에 전달
        PaymentRequestDto paymentRequestDto;

        paymentRequestDto = PaymentRequestDto.builder()
                .impCode(impCode) // 포트원에서 받은 앱 식별 코드
                .merchantUid(products.getSeller().getId() +"_"+ LocalDateTime.now()) // 결제 고유 번호 결제 시 고유 해야함
                .productTitle(products.getTitle()) // 상품 제목
                .productCategory(products.getCategory().getCategory())
                .productId(products.getProductId()) // 상품 고유 id
                .amount(products.getPrice()) // 상품 가격
                .buyerId(users.getId()) // 사용자의 고유 ID
                .buyerName(users.getNickname()) // 사용자의 닉네임
                .buyerTel(users.getPhone()) // 사용자의 전화번호
                .buyerEmail(users.getEmail()) // 사용자의 이메일
                .buyerAddr(users.getAddress()) // 사용자의 주소
                .build();

        return paymentRequestDto;
    }

}
