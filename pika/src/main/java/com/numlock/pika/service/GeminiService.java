package com.numlock.pika.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Tool;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions; // HttpOptions 임포트 추가
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private Client geminiClient;

    private final com.numlock.pika.repository.ProductRepository productRepository;

    // 세션별 대화 기록 저장소 (메모리)
    private final Map<String, List<Content>> chatHistories = new ConcurrentHashMap<>();

    public GeminiService(com.numlock.pika.repository.ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // API 키 유효성 검사 및 클라이언트 초기화 (애플리케이션 시작 시 1회)
    @PostConstruct
    public void init() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {

            throw new IllegalArgumentException("Gemini API Key가 설정되지 않았습니다. application.properties 또는 환경 변수에 'gemini.api.key'를 설정해주세요.");
        }
        this.geminiClient = Client.builder()
                .apiKey(geminiApiKey)
                .httpOptions(HttpOptions.builder().apiVersion("v1beta").build()) // API 버전을 v1beta로 설정 (Tools 사용 위해)
                .build();
    }

    /**
     * 상품 시세 분석 (RAG + Google Search Grounding)
     *
     * @param productId 분석할 상품 ID
     * @return 분석 결과 텍스트
     */
    public String analyzeProductPrice(int productId) {
        // 1. 상품 정보 조회
        com.numlock.pika.domain.Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));

        // 2. 상품명에서 핵심 키워드 추출 (AI)
        String refinedKeyword = extractSearchKeyword(product.getTitle());
        if ("NONE".equals(refinedKeyword)) {
            refinedKeyword = product.getTitle(); // 추출 실패 시 원본 제목 사용
        }

        // 3. 내부 평균 시세 조회 (정제된 키워드 + 카테고리 기준)
        Double internalAvg = productRepository.findAveragePriceByTitleAndCategory(
                refinedKeyword,
                product.getCategory().getCategoryId()
        );

        String internalInfo = (internalAvg != null)
                ? String.format("%,.0f원", internalAvg)
                : "정보 없음";

        // 4. 프롬프트 구성
        String prompt = String.format(
                "다음 상품에 대한 시세 분석을 수행하고, 결과를 아래의 **출력 형식**에 맞춰서 간결하고 가독성 있게 작성해줘.\n" +
                        "불필요한 서론이나 인사는 생략해.\n\n" +
                        "[상품 정보]\n" +
                        "1. 게시글 제목: %s\n" +
                        "2. 핵심 상품명: %s\n" +
                        "3. 판매 희망가: %,.0f원\n" +
                        "4. 마켓(Pika) 내 평균 거래가: %s\n\n" +
                        "[지시사항]\n" +
                        "- 구글 검색 도구를 사용하여 '%s'의 '정가(신품가)'와 '현재 온라인 중고 시세'를 찾아줘.\n\n" +
                        "[출력 형식]\n" +
                        "### 🏷️ [%s] 분석 결과\n\n" +
                        "**💰 가격 정보**\n" +
                        "- **판매 희망가:** (판매 희망가)원\n" +
                        "- **정가(신품가):** (검색된 정가, 모르면 '정보 없음')\n" +
                        "- **중고 시세:** (검색된 중고 시세 범위)\n" +
                        "- **Pika 내 평균:** (마켓 내 평균 거래가)\n\n" +
                        "**📊 분석 및 코멘트**\n" +
                        "- **상품 요약:** (상품에 대한 1줄 설명)\n" +
                        "- **가격 분석:** (판매 희망가가 시세 대비 어떤지, 구매/판매 추천 여부를 2~3문장으로 핵심만 요약)",
                product.getTitle(),
                refinedKeyword,
                product.getPrice(),
                internalInfo,
                refinedKeyword,
                refinedKeyword
        );

        try {
            // 5. Google Search 도구 설정
            Tool googleSearchTool = Tool.builder()
                    .googleSearch(GoogleSearch.builder().build())
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .tools(Arrays.asList(googleSearchTool))
                    .temperature(0.2f) // 사실 기반 분석을 위해 온도를 낮게 설정 (창의성 억제)
                    .maxOutputTokens(2500)
                    .build();

            // 6. Gemini 호출
            GenerateContentResponse response = geminiClient.models.generateContent(
                    "models/gemini-2.5-flash",
                    Content.builder().parts(Collections.singletonList(Part.builder().text(prompt).build())).build(),
                    config
            );

            if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                return response.text();
            }

        } catch (Exception e) {
            System.err.println("Gemini Analysis 오류: " + e.getMessage());
            return "죄송합니다. 시세 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        return "시세 정보를 가져오지 못했습니다.";
    }

    /**
     * 사용자의 채팅 메시지에 대한 답변을 생성합니다.
     * 대화 맥락(Context)을 유지하며 답변합니다.
     *
     * @param sessionId   사용자 세션 ID
     * @param userMessage 사용자 메시지
     * @return AI 답변
     */
    public String getChatResponse(String sessionId, String userMessage) {
        try {
            // 해당 세션의 대화 기록 가져오기 (없으면 생성)
            List<Content> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());

            String context = "";
            // 가격 관련 키워드가 있는지 확인
            if (userMessage.contains("얼마") || userMessage.contains("가격") || userMessage.contains("시세") || userMessage.contains("적정가")) {
                String keyword = extractSearchKeyword(userMessage);
                if (!"NONE".equals(keyword)) {
                    // DB에서 상품 검색 (최신순 5개)
                    org.springframework.data.domain.Page<com.numlock.pika.domain.Products> products =
                            productRepository.searchByFilters(keyword, null, org.springframework.data.domain.PageRequest.of(0, 5));

                    if (products.hasContent()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("현재 마켓에 올라온 '").append(keyword).append("' 관련 상품 정보입니다:\n");
                        for (com.numlock.pika.domain.Products p : products.getContent()) {
                            sb.append("- ").append(p.getTitle()).append(": ").append(p.getPrice()).append("원\n");
                        }
                        context = sb.toString();
                    } else {
                        context = "마켓에 '" + keyword + "' 관련 상품이 현재 없습니다.";
                    }
                }
            }

            // 시스템 프롬프트 성격의 지시문과 컨텍스트, 사용자 질문을 조합
            String finalPromptText = "";
            if (history.isEmpty()) {
                finalPromptText += "당신은 중고거래 마켓 'Pika'의 친절한 AI 어시스턴트입니다. 사용자의 질문에 답변해주세요.\n";
            }
            if (!context.isEmpty()) {
                finalPromptText += "참고할 마켓 데이터:\n" + context + "\n";
            }
            finalPromptText += userMessage;

            // 사용자 메시지를 Content 객체로 생성하여 History에 추가
            Content userContent = Content.builder()
                    .role("user")
                    .parts(Collections.singletonList(Part.builder().text(finalPromptText).build()))
                    .build();
            history.add(userContent);

            // Gemini API 호출 (전체 히스토리 전달)
            GenerateContentConfig chatConfig = GenerateContentConfig.builder()
                    .maxOutputTokens(3000)
                    .temperature(0.7f)
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", history, chatConfig);

            if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                // 토큰 사용량 로그 출력
                if (response.usageMetadata().isPresent()) {
                    var usage = response.usageMetadata().get();
                    System.out.println("=== [Chat] Token Usage ===");
                    System.out.println("Input Tokens : " + usage.promptTokenCount());
                    System.out.println("Output Tokens: " + usage.candidatesTokenCount());
                    System.out.println("Total Tokens : " + usage.totalTokenCount());
                    System.out.println("==========================");
                }

                String responseText = response.text();

                // 모델의 응답을 Content 객체로 생성하여 History에 추가
                Content modelContent = Content.builder()
                        .role("model")
                        .parts(Collections.singletonList(Part.builder().text(responseText).build()))
                        .build();
                history.add(modelContent);

                return responseText;
            }
        } catch (Exception e) {
            System.err.println("Gemini Chat 오류: " + e.getMessage());
            // 오류 발생 시, 방금 추가한 사용자 메시지는 제거하는 것이 좋을 수 있음 (선택 사항)
            return "죄송합니다. 현재 답변을 생성할 수 없습니다.";
        }
        return "죄송합니다. 이해하지 못했습니다.";
    }

    private String extractSearchKeyword(String userMessage) {
        try {
            String prompt = "다음 문장에서 검색할 상품명 키워드만 딱 하나 추출해줘. 조사나 불필요한 말은 빼고 명사 위주로. 없으면 NONE 이라고만 출력해.\n문장: " + userMessage;

            GenerateContentConfig keywordConfig = GenerateContentConfig.builder()
                    .maxOutputTokens(50)
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", prompt, keywordConfig);
            if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                if (response.usageMetadata().isPresent()) {
                    var usage = response.usageMetadata().get();
                    System.out.println("[Keyword] Tokens - In: " + usage.promptTokenCount() + ", Out: " + usage.candidatesTokenCount());
                }
                return response.text().trim();
            }
        } catch (Exception e) {
            System.err.println("키워드 추출 오류: " + e.getMessage());
        }
        return "NONE";
    }

    /**
     * 판매자 리뷰 목록을 기반으로 효율적인 한줄평을 생성합니다. (최적화 버전)
     */
    public String generateReviewSummary(List<String> reviewContents) {
        // 1. 리뷰가 없거나 null인 경우 즉시 반환하여 불필요한 API 호출 방지
        if (reviewContents == null || reviewContents.isEmpty()) {
            return "아직 등록된 리뷰가 없습니다.";
        }

        // 2. 최신순 리뷰 10개로 제한하여 토큰 사용량 최소화 및 성능 향상
        // (리뷰가 아무리 많아도 이 범위를 넘지 않아 안정적입니다)
        List<String> limitedReviews = reviewContents.stream()
                .limit(10)
                .collect(Collectors.toList());

        // 3. 리뷰 내용을 하나의 문자열로 결합
        String combinedReviews = String.join("\n", limitedReviews);

        // 4. 예외 상황(데이터 부족 등)까지 고려한 강화된 프롬프트
        String prompt = "다음은 판매자에 대한 실제 고객 리뷰들입니다. 이를 종합하여 판매자의 특징을 50자 이내의 한줄평으로 요약해주세요. " +
                "만약 리뷰 내용이 짧아 요약이 어렵다면 '정보가 부족한 판매자입니다'라고 출력하세요. " +
                "글자 수나 기호는 포함하지 마세요:\n\n" + combinedReviews;

        try {
            // 5. 응답 속도를 위해 설정을 간소화하여 호출
            GenerateContentConfig summaryConfig = GenerateContentConfig.builder()
                    .maxOutputTokens(400) // 한줄평이므로 출력 토큰을 낮춰 비용 절감
                    .temperature(0.5f)    // 적당한 일관성 유지
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", prompt, summaryConfig);

            // 6. 결과 추출 및 반환
            if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                return response.text().trim();
            }
        } catch (Exception e) {
            // 7. 에러 발생 시 원인을 구체적으로 로그에 남김
            System.err.println("Gemini 요약 API 오류: " + e.getMessage());
            return "리뷰를 분석 중입니다. 잠시 후 확인해 주세요.";
        }

        return "리뷰 요약을 생성할 수 없습니다.";
    }
}
