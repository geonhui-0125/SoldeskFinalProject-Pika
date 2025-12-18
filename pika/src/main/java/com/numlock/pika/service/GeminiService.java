package com.numlock.pika.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions; // HttpOptions 임포트 추가
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private Client geminiClient;

    // API 키 유효성 검사 및 클라이언트 초기화 (애플리케이션 시작 시 1회)
    @PostConstruct
    public void init() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            // 실제 배포 환경에서는 이 예외를 처리하고 애플리케이션이 안전하게 시작되도록 해야 합니다.
            // 여기서는 개발 환경을 위해 간단히 예외를 발생시킵니다.
            throw new IllegalArgumentException("Gemini API Key가 설정되지 않았습니다. application.properties 또는 환경 변수에 'gemini.api.key'를 설정해주세요.");
        }
        this.geminiClient = Client.builder()
                .apiKey(geminiApiKey)
                .httpOptions(HttpOptions.builder().apiVersion("v1").build()) // API 버전을 v1으로 설정
                .build();
    }

    /**
     * 판매자 리뷰 목록을 기반으로 한줄평을 생성합니다.
     *
     * @param reviewContents 판매자에 대한 리뷰 내용(String) 목록
     * @return 생성된 한줄평 또는 오류 메시지
     */
    public String generateReviewSummary(List<String> reviewContents) {
        if (reviewContents == null || reviewContents.isEmpty()) {
            return "아직 리뷰가 없습니다."; // 리뷰가 없으면 기본 메시지 반환
        }

        // 모든 리뷰 내용을 하나의 문자열로 결합합니다. 각 리뷰는 새 줄로 구분합니다.
        String combinedReviews = reviewContents.stream()
                .collect(Collectors.joining("\n"));

        // Gemini 모델에 전달할 프롬프트를 구성합니다.
        // 판매자 리뷰들을 읽고, 이 판매자에 대한 50자 이내의 한줄평을 작성해달라고 지시합니다.
        // 긍정적이고 핵심적인 내용을 위주로 요약하고, 판매자의 특징을 잘 나타내도록 요청합니다.
        String prompt = "다음 판매자 리뷰들을 읽고, 이 판매자에 대한 50자 이내의 한줄평을 작성해줘. 핵심적인 내용을 위주로 요약하고, 판매자의 특징을 잘 나타내도록 해줘, 글자수 표시는 하지마 :\n\n" + combinedReviews;

        
        try {
            // Gemini API를 호출하여 요약을 생성합니다.
            // gemini-1.5-flash 모델은 빠르고 비용 효율적이므로 우선적으로 사용합니다.
            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", prompt, null);

            // API 응답을 파싱하여 생성된 한줄평을 추출합니다.
            if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                return response.text();
            }
        } catch (Exception e) {
            // API 호출 중 오류 발생 시, 오류 메시지를 콘솔에 출력하고 기본 오류 메시지를 반환합니다.
            System.err.println("Gemini API 호출 중 오류 발생: " + e.getMessage());
            return "리뷰 요약 생성 중 오류가 발생했습니다.";
        }

        // 응답이 유효하지 않은 경우
        return "리뷰를 요약할 수 없습니다.";
    }

}
