package com.numlock.pika.service.user;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Users;
import com.numlock.pika.repository.AccountRepository;
import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository; // 사용자 조회를 위해 필요

    /**
     * 은행 리스트 반환 (코드 및 이름 매핑)
     */
    public List<Map<String, String>> getBankList() {
        return List.of(
                Map.of("code", "004", "name", "KB국민", "len", "14"),
                Map.of("code", "088", "name", "신한", "len", "12"),
                Map.of("code", "020", "name", "우리", "len", "13"),
                Map.of("code", "081", "name", "하나", "len", "13"),
                Map.of("code", "003", "name", "IBK기업", "len", "13"),
                Map.of("code", "011", "name", "NH농협", "len", "13"),
                Map.of("code", "090", "name", "카카오뱅크", "len", "10"),
                Map.of("code", "092", "name", "토스뱅크", "len", "13"),
                Map.of("code", "089", "name", "케이뱅크", "len", "12")
        );
    }

    @Transactional
    public void saveOrUpdateAccount(Accounts accountRequest, String sellerId) {
        // 판매자 조회
        Users seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 계좌 정보 가져오기 (분리된 메서드 호출)
        Accounts accounts = getOrCreateAccount(seller);

        System.out.println("은행 코드 확인 : " +  accountRequest.getBankCode());
        System.out.println("은행 코드 타입 확인 : " + accountRequest.getBankCode().getClass().getName());

        // 엔티티 데이터 세팅
        accounts.setBankCode(accountRequest.getBankCode());
        // 숫자만 추출하여 저장 (데이터 정규화)
        accounts.setAccountNumber(accountRequest.getAccountNumber().replaceAll("[^0-9]", ""));

        // 저장 (JPA 변경 감지 기능 덕분에 새로운 객체일 때만 명시적 save가 필요할 수 있음)
        accountRepository.save(accounts);
    }

    /**
     * 계좌 정보를 조회하고, 없으면 새로 생성하여 반환하는 메서드
     */
    @Transactional(readOnly = true)
    public Accounts getOrCreateAccount(Users seller) {
        // 1. 엔티티 관계에 따라 findBySeller를 사용하는 것이 더 안전합니다.
        return accountRepository.findBySeller(seller)
                .orElseGet(() -> {
                    // 2. 계좌가 없는 신규 유저를 위해 빈 객체 생성 및 판매자 연결
                    Accounts newAccount = new Accounts();
                    newAccount.setSeller(seller);
                    // 기본값 세팅이 필요하다면 여기서 수행
                    return newAccount;
                });
    }
}