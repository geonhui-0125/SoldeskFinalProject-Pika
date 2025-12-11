package com.numlock.pika.service.payment;

import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final DefaultMessageService messageService;

    @Autowired
    public SmsService(@Value("${coolsms.apiKey}") String apiKey,
                      @Value("${coolsms.secretkey}") String secretKey) {

        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, secretKey, "http://api.coolsms.co.kr");
    }

    public void sendMessage() {
        Message message = new Message();
        message.setFrom("01094063580");
        message.setTo("01094063580");
        message.setText("한정판 피규어 A 판매 완료\n");

        SingleMessageSentResponse response = this.messageService.sendOne((new SingleMessageSendingRequest(message)));
        System.out.println(response);
    }

}
