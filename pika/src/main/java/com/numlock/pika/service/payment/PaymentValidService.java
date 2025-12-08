package com.numlock.pika.service.payment;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.extern.slf4j.Slf4j;
import com.numlock.pika.domain.Products;
import com.numlock.pika.dto.PaymentValidDto;
import com.numlock.pika.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class PaymentValidService {

    private final IamportClient iamportClient;

    @Autowired
    private ProductRepository productRepository;

    //생성자로 IamportClient 초기화 
    //불변성 보장, null값 방지
    //properties.application 파일의 포트원 키 정보 가져옴
    public PaymentValidService(@Value("${imp.access}") String apiKey,
                               @Value("${imp.secret}") String apiSecret) {

        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    /**
     * 결제 정보를 PortOne 서버와 비교하여 검증
     *
     * @param PaymentVaildDto 클라이언트에서 받은 아임포트 결제 정보
     * @return 검증된 결제 응답
     * @throws IamportResponseException
     * @throws IOException
     */

    //BigDecimal : 매우 정밀하고 정확한 십진수 연산을 수행하기 위해 제공되는 클래스
    public IamportResponse<Payment> validatePayment(PaymentValidDto paymentVaildDto)
            throws IamportResponseException, IOException {

        // 결제 검증 데이터 impUid, amount 정의
        String impUid =  paymentVaildDto.getImpUid();
        BigDecimal amount = paymentVaildDto.getAmount();

        
        // 상품 데이터 데이터베이스 조회
        // taskId(상품 Id) 로 조회 findById 
        // Optional<> orElseThrow로 상품이 존재하지 않으면 에러 생성
        Optional<Products> productOptional = productRepository.findById(paymentVaildDto.getTaskId());
        Products product = productOptional.orElseThrow(
                () -> new IllegalArgumentException("해당 상품은 존재하지 않습니다."));

        //포트원으로 부터 클라이언트가 결제한 impUid(결제 고유 ID)를 전달해 클라이언트 결제 정보 응답 
        IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(impUid);
        Payment paymentData = iamportResponse.getResponse();

        // 검증 로직: 실제 결제 금액(데이터베이스 저장된 상품 금액)과 서버가 기대하는 금액(클라이언트가 결제한 금액) 비교
        if (paymentData != null) {
            BigDecimal actualAmount = product.getPrice();

            System.out.println("데이터 베이스 상품 가격 : " + actualAmount);

            // PortOne에서 받은 실제 결제 금액(actualAmount)이 서버가 기대하는 금액(amount)과 일치하는지 확인
            if (!amount.equals(actualAmount)) {
                System.out.println("결제 금액 일치 확인");
                System.out.println("impUid : " + impUid);
                System.out.println("amount : " + amount);
                System.out.println("actualAmount : " + actualAmount);

                // 금액 불일치 시, 즉시 결제 취소(환불) 로직 호출
                cancelPayment(impUid);

                throw new RuntimeException("결제 금액 불일치");
            }
        } else {
            // 결제 데이터가 존재하지 않음 (오류) , 즉시 결제 취소(환불) 로직 호출
            cancelPayment(impUid);

            throw new RuntimeException("유효하지 않은 결제 정보입니다.");
        }

        return iamportResponse;
    }

    //주문 결제 환불/취소 시
    public void cancelPayment(String impUid) throws IamportResponseException, IOException {

        CancelData cancelData = new CancelData(impUid, true); // amount를 생략하면 전액 취소

        IamportResponse<Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

        if (cancelResponse.getResponse() != null) {
            System.out.println("결제 취소 성공 : " + cancelResponse.getResponse());
        } else {
            throw new RuntimeException("결제 취소 실패 : " + cancelResponse.getMessage());
        }
    }

}
