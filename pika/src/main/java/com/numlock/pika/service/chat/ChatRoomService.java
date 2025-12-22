package com.numlock.pika.service.chat;

import com.numlock.pika.domain.Message;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ChatRoomDto;
import com.numlock.pika.repository.MessageRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public List<ChatRoomDto> getChatRoomList(String userId) {
        List<Message> latestMessages = messageRepository.findLatestMessagesPerChatRoom(userId);
        List<ChatRoomDto> chatRooms = new ArrayList<>();

        for (Message message : latestMessages) {
            String otherUserId = message.getSenderId().equals(userId) ? message.getRecipientId() : message.getSenderId();
            Users otherUser = userRepository.findById(otherUserId)
                    .orElse(null); // Or handle more gracefully

            if (otherUser == null) continue; // Skip if other user not found

            Products product = message.getProduct();
            long unreadCount = messageRepository.countByProductProductIdAndRecipientIdAndSenderIdAndIsRead(product.getProductId(), userId, otherUserId, false);

            List<String> imageURLs = productService.getImageUrls(product.getProductImage());

            chatRooms.add(ChatRoomDto.builder()
                    .productId(product.getProductId())
                    .productTitle(product.getTitle())
                    .productImage(imageURLs.isEmpty() ? null : imageURLs.get(0))
                    .otherUserId(otherUser.getId())
                    .otherUserNickname(otherUser.getNickname())
                    .otherUserProfileImage(otherUser.getProfileImage())
                    .lastMessage(message.getContent())
                    .lastMessageTime(message.getSentAt())
                    .unreadCount(unreadCount)
                    .build());
        }

        return chatRooms;
    }
}
