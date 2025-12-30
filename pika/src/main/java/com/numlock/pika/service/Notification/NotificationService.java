package com.numlock.pika.service.Notification;

import com.numlock.pika.handler.NotificationWebSocketHandler; // NotificationWebSocketHandler import 추가
import com.numlock.pika.domain.*;
import com.numlock.pika.dto.NotificationDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.dto.payment.PaymentResDto;
import com.numlock.pika.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final FavoriteProductRepository favoriteProductRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler; // 핸들러 주입

    /**
     * 1. 찜한 상품 가격 변동 (다수 유저 대상)
     * 상황: 찜한 상품이 가격이 변동된 경우
     */
    @Transactional
    public void sendProductChange(int productId, ProductRegisterDto dto) {

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        List<Users> receivers = favoriteProductRepository.findByProduct(product).stream()
                .map(FavoriteProducts::getUser).toList();

        BigDecimal oldPrice = product.getPrice();
        String productName = product.getTitle();
        BigDecimal newPrice = dto.getPrice();

        if (receivers.isEmpty()) return;

        String title;
        String content;

        if(!oldPrice.equals(newPrice)) {
            title = "찜한 상품 가격 변동";
            content = String.format("'%s'의 가격이 %s원에서 %s원으로 변동되었습니다.", productName, oldPrice, newPrice); /*상품의 가격이 변동된 경우*/
        }else {
            title = "찜한 상품 정보 변동";
            content = String.format("%s의 정보가 변동되었습니다.", productName); /*상품의 가격이 변동 안된 경우 정보만 바뀐 경우*/
        }

        String actionUrl = "/products/info/" + productId;

        // 리스트를 한꺼번에 생성해서 저장
        saveAll(receivers, "PRODUCT_CHANGE", title, content, actionUrl);
    }

    /**
     * 2. 찜한 상품 판매 완료 (다수 유저 대상)
     * 상황: 찜한 상품이 다른이에게 판매가 완료된 경우
     */
    @Transactional
    public void sendSoldOut(String impUid) {

        Payments payments = paymentRepository.findById(impUid)
                .orElseThrow(() -> new RuntimeException("해당 결제 내역을 찾을 수 없습니다"));

        Products product = productRepository.findById(payments.getTask().getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        //찜 목록에서 본인 제외하고 수신자 추출
        List<Users> receivers = favoriteProductRepository.findByProduct(product).stream()
                .map(FavoriteProducts::getUser) // FavoriteProducts 단계에서 미리 거르기
                .filter(user -> !user.getId().equals(currentUserId))
                .toList();

        String productName = product.getTitle();

        if (receivers.isEmpty()) return;

        String title = "찜한 상품 판매 완료";
        String content = String.format("아쉬워요! 찜하신 '%s' 상품이 판매 완료되었습니다.", productName);
        String actionUrl = "/products/info/" + product.getProductId();

        saveAll(receivers, "SOLD_OUT", title, content, actionUrl);
    }

    /**
     * 다수 유저에게 알림 저장 및 실시간 전송
     */
    private void saveAll(List<Users> receivers, String type, String title, String content, String url) {
        List<Notifications> notiList = receivers.stream()
                .map(user -> Notifications.builder()
                        .receiver(user)
                        .type(type)
                        .title(title)
                        .content(content)
                        .actionUrl(url)
                        .build())
                .collect(Collectors.toList());

        List<Notifications> savedNotiList = notificationRepository.saveAll(notiList);

        // 실시간 알림 전송
        savedNotiList.forEach(noti -> {
            NotificationDto notificationDto = NotificationDto.fromEntity(noti);
            notificationWebSocketHandler.sendNotification(noti.getReceiver().getId(), notificationDto);
        });
    }

    /**
     * 3. 채팅 알림 (메시지 전송 시 호출)
     * 상황: 판매자 구매자 양쪽 거래 전 상황에서
     */
    @Transactional
    public void sendChatNoti(String senderId, String recipientId, String msg, Integer productId) {

        Users receiver = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        Users sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String title = "채팅 알림";
        String content = String.format("%s님으로 부터 채팅이 왔습니다.", sender.getNickname());
        String actionUrl = "/chat/dm?recipientId=" + senderId + "&productId=" + productId ;

        save(receiver, "CHAT", title, content, actionUrl);
    }

    /**
     * 4. 내 상품 판매 완료 (판매자용)
     * 상황: 판매의 상품의 판매(구매 확정)가 완료되었을 때
     */
    @Transactional
    public void sendSellerSoldOut(String impUid) {

        Payments payments = paymentRepository.findById(impUid)
                .orElseThrow(() -> new RuntimeException("해당 결제 내역을 찾을 수 없습니다"));

        Products product = productRepository.findById(payments.getTask().getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        String productName = product.getTitle();
        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String buyerNick = users.getNickname();

        String title = "상품 판매 완료!";
        // 판매자에게 축하 메시지와 함께 구매자 정보를 보여줌
        String content = String.format("축하합니다! '%s' 상품이 %s님에게 판매되었습니다.",
                productName, buyerNick);

        // 상품 상세 페이지로 연결
        String actionUrl = "/trade/history";

        save(product.getSeller(), "SELL_DONE", title, content, actionUrl);
    }

    /**
     * 5. 내 상품 거래 중 (판매자용)
     * 상황: 판매의 상품의 결제(결제하기)가 완료되었을 때
     */
    @Transactional
    public void sendSellerApproval(PaymentResDto paymentResDto) {

        Products product = productRepository.findById(paymentResDto.getTaskId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String productName = product.getTitle();
        String buyerNick = users.getNickname();

        String title = "상품 결제 대기중";
        // 판매자에게 구매자 정보를 보여줌
        String content = String.format("'%s' 상품이 %s님에게 결제 되었습니다. 판매 승인 후,상품을 전달해 주세요!",
                productName, buyerNick);
        // 상품 상세 페이지로 혹은 구매자 채팅으로 연결
        String actionUrl = "/products/info/"+ product.getProductId();

        save(product.getSeller(), "SELL_APPROVE", title, content, actionUrl);
    }

    /**
     * 6. 내 상품 거래 취소 (판매자용)
     * 상황: 판매의 상품의 결제(결제하기)가 취소되었을 때
     *//*
    @Transactional 무통장 결제라 취소가 안됨
    public void sendProductCancel(Users seller, String buyerNick, String productName, int productId) {

        String title = "내 상품 찜 알림";
        String content = String.format("%s님이 회원님의 '%s' 상품을 관심 상품으로 등록했습니다.",
                buyerNick, productName);
        String actionUrl = "/product/detail?id=" + productId;

        save(seller, "ITEM_LIKED", title, content, actionUrl);
    }*/
    
    /**
     * 7. 누군가 내 상품을 찜했을 때 (판매자용)
     * 상황: 구매자가 '찜' 버튼을 눌렀을 때 
     */
    @Transactional
    public void sendSellerProductWished(int productId, Principal principal) {

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        String productName = product.getTitle();

        Users users = userRepository.findById(principal.getName())
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String buyerNick = users.getNickname();

        String title = "내 상품 찜 알림";
        String content = String.format("%s님이 회원님의 '%s' 상품을 관심 상품으로 등록했습니다.",
                buyerNick, productName);

        // 상품 상세 페이지로 이동해서 찜 개수 확인
        String actionUrl = "/products/info/" + productId;

        save(product.getSeller(), "PRODUCT_WISHED", title, content, actionUrl);
    }

    /**
     * 8. 구매자가 구매 확정 후 리뷰페이지로 이동 (구매자용)
     * 상황: 구매자 구매 확정 버튼을 눌렀을 때
     */
    @Transactional
    public void sendBuyerProductReview(String impUid) {

        Payments payments = paymentRepository.findById(impUid)
                .orElseThrow(() -> new RuntimeException("해당 결제 내역을 찾을 수 없습니다"));

        Products product = productRepository.findById(payments.getTask().getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        String productName = product.getTitle();

        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String title = "상품 리뷰 작성 알림";
        String content = String.format("'%s' 상품 거래는 어떠셨나요? 거래 후기 작성해주세요!",
                productName);

        // 상품 상세 페이지로 이동해서 찜 개수 확인
        String actionUrl = "/reviews/new?productId=" + product.getProductId() + "&sellerId=" + product.getSeller().getId() + "&sellerNickname=" + product.getSeller().getNickname();

        save(users, "PRODUCT_REVIEW", title, content, actionUrl);
    }

    /**
     * 9. 내 상품 거래 중 (구매자용)
     * 상황: 판매의 상품의 결제(결제하기)가 완료되었을 때
     */
    @Transactional
    public void sendBuyerDealing(PaymentResDto paymentResDto) {

        Products product = productRepository.findById(paymentResDto.getTaskId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        Payments payments = paymentRepository.findByTask(product)
                .orElseThrow(() -> new RuntimeException("결제 내역을 찾을 수 없습니다"));

        String productName = product.getTitle();

        String title = "상품 거래중";
        // 판매자에게 구매자 정보를 보여줌
        String content = String.format("'%s' 상품이 판매 승인 되었습니다. 상품을 수령 후 구매 확정을 눌려주세요!",
                productName);
        // 상품 상세 페이지로 혹은 구매자 채팅으로 연결
        String actionUrl = "/products/info/"+ product.getProductId();

        save(payments.getBuyer(), "SELL_DEAL", title, content, actionUrl);
    }

    /**
     * 소수 유저에게 알림 저장 및 실시간 전송
     */
    private void save(Users receiver, String type, String title, String content, String url) {

        Notifications noti = Notifications.builder()
                .receiver(receiver)
                .type(type)
                .title(title)
                .content(content)
                .actionUrl(url)
                .build();

        Notifications savedNoti = notificationRepository.save(noti);

        // 실시간 알림 전송
        NotificationDto notificationDto = NotificationDto.fromEntity(savedNoti);
        notificationWebSocketHandler.sendNotification(savedNoti.getReceiver().getId(), notificationDto);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUnreadNotifications(String username, Pageable pageable) {
        Users user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Page<Notifications> notifications = notificationRepository.findByReceiverAndIsReadOrderByCreatedAtDesc(user, 0, pageable);

        return notifications.map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(String username) {
        Users user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return notificationRepository.countByReceiverAndIsRead(user, 0);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications(String username) {
        Users user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteByNotificationId(notificationId);
    }

    @Transactional
    public void deleteAllNotifications(String username) {
        Users user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        notificationRepository.deleteByReceiver(user);
    }

}

