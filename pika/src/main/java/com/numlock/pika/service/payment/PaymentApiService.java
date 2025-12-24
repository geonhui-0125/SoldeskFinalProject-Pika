package com.numlock.pika.service.payment;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Payments;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.payment.PaymentResDto;
import com.numlock.pika.repository.AccountRepository;
import com.numlock.pika.repository.PaymentRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentApiService {

    private final IamportClient iamportClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmsService smsService;

    //생성자로 IamportClient 초기화
    //불변성 보장, null값 방지
    //properties.application 파일의 포트원 키 정보 가져옴
    public PaymentApiService(@Value("${imp.access}") String apiKey,
                             @Value("${imp.secret}") String apiSecret) {

        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    /**
     * 결제 정보를 PortOne 서버와 비교하여 검증
     * @param PaymentValidDto 클라이언트에서 받은 아임포트 결제 정보
     * @return 검증된 결제 응답
     * @throws IamportResponseException
     * @throws IOException
     */

    @Transactional
    //BigDecimal : 매우 정밀하고 정확한 십진수 연산을 수행하기 위해 제공되는 클래스
    public IamportResponse<Payment> validatePayment(PaymentResDto paymentResDto)
            throws IamportResponseException, IOException {

        // 결제 검증 데이터 impUid, amount 초기화
        String impUid =  paymentResDto.getImpUid();
        BigDecimal amount = paymentResDto.getAmount();

        // 상품 데이터 데이터베이스 조회
        // taskId(상품 Id) 로 조회 findById
        // Optional<> orElseThrow로 상품이 존재하지 않으면 에러 생성
        Optional<Products> productOptional = productRepository.findById(paymentResDto.getTaskId());
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
                System.out.println("결제 금액 불일치 확인");
                System.out.println("impUid : " + impUid);
                System.out.println("amount : " + amount);
                System.out.println("actualAmount : " + actualAmount);

                throw new RuntimeException("결제 금액이 서버와 불일치 합니다.");
            }
            // 만약 금액이 일치하면 데이터 베이스에 결제 정보 저장
            savePaymentInfo(paymentResDto);

        } else {
            // 결제 데이터가 존재하지 않음 (오류) 결제가 안된 경우일 확률이 높아서 안돌아갈 가능성이 높음
            throw new RuntimeException("유효하지 않은 결제 정보입니다.");
        }

        return iamportResponse;
    }

    @Transactional
    public void savePaymentInfo(PaymentResDto paymentResDto) {

        Products products = productRepository.findById(paymentResDto.getTaskId())
                .orElseThrow(() -> new RuntimeException("해당 상품을 찾을 수 없습니다"));

        Users buyer = userRepository.findById(paymentResDto.getBuyerId())
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다"));

        Payments payment = Payments.builder()
                .impUid(paymentResDto.getImpUid())
                .merchantUid(paymentResDto.getMerchantUid())
                .task(products)
                .amount(paymentResDto.getAmount())
                .seller(products.getSeller())
                .buyer(buyer)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // 상품 상태를 '결제 진행중'(2)으로 변경
        Products product = productRepository.findById(paymentResDto.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품은 존재하지 않습니다. ID: " + paymentResDto.getTaskId()));
        product.setProductState(3);
        productRepository.save(product);
    }

    //주문 결제 확정 시
    @Transactional
    public String confirmPayment(String impUid) throws IamportResponseException, IOException {

        //결제 정보 찾기
        Payments payments = paymentRepository.findById(impUid)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 상품 상태를 '판매완료'(1)로 변경
        Products product = productRepository.findById(payments.getTask().getProductId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품은 존재하지 않습니다. ID: " + payments.getTask()));
        product.setProductState(1);
        productRepository.save(product);

        System.out.println("payments 확인 : " + payments);

        //결제한 정보에서 판매자 UID 찾기 (merchantUID 형태 : 판매자UID_202512080404)
        String mUid = payments.getMerchantUid().split("_")[0];

        System.out.println("판매자 id 확인 : " + mUid);

        //해당 사용자의 은행 계좌 정보 가져옴
        Accounts accounts = accountRepository.findByUserId(mUid)
                .orElseThrow(() -> new RuntimeException("해당 사용자의 계좌 정보가 존재하지 않습니다."));

        //결제 취소/환불 요청 데이터에 판매자 계좌 정보로 수정
        System.out.println("예금주명 : " + accounts.getSeller());
        System.out.println("은행 코드 : " + accounts.getBankCode());
        System.out.println("계좌 번호 : " + accounts.getAccountNumber());

        // 구매 확정을 누르면 판매자에게 메시지를 전송
        /*smsService.sendMessage();*/

        return "구매 확정이 완료되었습니다";

    }

    public int cancelPayment(String impUid) {

        Payments payments = paymentRepository.findById(impUid)
                .orElseThrow(() -> new RuntimeException("결제 내역을 찾을 수 없습니다."));

        System.out.println("payments : " + payments);

        Products product = productRepository.findById(payments.getTask().getProductId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품은 존재하지 않습니다. ID: " + payments.getTask()));

        product.setProductState(0);
        productRepository.save(product);

        paymentRepository.delete(payments);

        return product.getProductId();
    }

    public int approvePayment(int productId) {

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품은 존재하지 않습니다. ID: " + productId));

        product.setProductState(2);
        productRepository.save(product);

        return product.getProductId();
    }

}
