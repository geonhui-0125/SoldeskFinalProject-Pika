package com.numlock.pika.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Tool;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
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
     * 상품 시세 분석 (MCP/Function Calling + Google Search Grounding)
     * AI가 스스로 DB 조회 도구를 사용해 정보를 얻고, 구글 검색을 병행하여 분석합니다.
     *
     * @param productId 분석할 상품 ID
     * @return 분석 결과 텍스트
     */
    public String analyzeProductPrice(int productId) {
        // 대화 기록 (이 분석 요청만을 위한 임시 히스토리)
        List<Content> history = new ArrayList<>();

        // 1. 도구 정의 (분리)
        Tool dbTool = Tool.builder()
                .functionDeclarations(Collections.singletonList(
                        FunctionDeclaration.builder()
                                .name("get_product_detail")
                                .description("상품 ID를 입력받아 상품의 상세 정보(제목, 가격, 카테고리)와 Pika 마켓 내 동일 카테고리/키워드 평균 거래가를 조회합니다.")
                                .parameters(
                                        Schema.builder()
                                                .type(Type.Known.OBJECT)
                                                .properties(Map.of(
                                                        "productId", Schema.builder()
                                                                .type(Type.Known.STRING)
                                                                .description("분석할 상품의 ID (숫자)")
                                                                .build()
                                                ))
                                                .required(Collections.singletonList("productId"))
                                                .build()
                                )
                                .build()
                ))
                .build();

        Tool googleSearchTool = Tool.builder()
                .googleSearch(GoogleSearch.builder().build())
                .build();

        // 2. 프롬프트 구성 (명확한 지시)
        String prompt = String.format(
                "상품 ID '%d'번에 대한 시세 분석을 시작해. 먼저 `get_product_detail` 도구를 사용해 DB에서 상품 정보를 가져와줘.",
                productId
        );

        history.add(Content.builder().role("user").parts(Collections.singletonList(Part.builder().text(prompt).build())).build());

        try {
            // [1단계] DB 조회 도구만 활성화
            GenerateContentConfig dbConfig = GenerateContentConfig.builder()
                    .tools(Collections.singletonList(dbTool))
                    .temperature(0.1f) // 함수 호출의 정확도를 위해 낮음
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", history, dbConfig);

            // 함수 호출 처리 루프
            if (response != null && response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                com.google.genai.types.Candidate candidate = response.candidates().get().get(0);

                if (candidate.content().isPresent()) {
                    history.add(candidate.content().get());
                }

                List<Part> parts = Collections.emptyList();
                if (candidate.content().isPresent() && candidate.content().get().parts().isPresent()) {
                    parts = candidate.content().get().parts().get();
                }

                boolean functionCalled = false;
                List<Part> functionResponseParts = new ArrayList<>();

                for (Part part : parts) {
                    if (part.functionCall().isPresent()) {
                        FunctionCall call = part.functionCall().get();

                        if (call.name().isPresent() && "get_product_detail".equals(call.name().get())) {
                            functionCalled = true;
                            Map<String, Object> args = call.args().orElse(Collections.emptyMap());
                            String idStr = (String) args.get("productId");

                            System.out.println("=== [Analyze Tool] Gemini requests function: get_product_detail(" + idStr + ") ===");

                            String dbResult = executeProductDetailSearch(idStr);

                            functionResponseParts.add(Part.builder()
                                    .functionResponse(FunctionResponse.builder()
                                            .name(call.name().get())
                                            .response(Map.of("result", dbResult))
                                            .build())
                                    .build());
                        }
                    }
                }

                if (functionCalled) {
                    // 함수 결과를 모델에게 전달
                    Content functionResponseContent = Content.builder()
                            .role("function")
                            .parts(functionResponseParts)
                            .build();
                    history.add(functionResponseContent);

                    // [2단계] 구글 검색 도구로 교체하여 최종 분석 요청
                    // 모델에게 이제 검색하고 분석하라는 추가 지시를 내림 (Context 유지)
                    String finalPrompt = "DB에서 확인된 상품명(키워드)을 바탕으로 구글 검색을 수행하여 '정가'와 '중고 시세'를 찾고, " +
                            "수집한 정보를 종합하여 아래 **출력 형식**에 맞춰 분석 보고서를 작성해줘.\n\n" +
                            "[출력 형식]\n" +
                            "### 🏷️ [상품명] 분석 결과\n\n" +
                            "**💰 가격 정보**\n" +
                            "- **판매 희망가:** (판매 희망가)원\n" +
                            "- **정가(신품가):** (검색된 정가, 모르면 '정보 없음')\n" +
                            "- **중고 시세:** (검색된 중고 시세 범위)\n" +
                            "- **Pika 내 평균:** (DB에서 조회한 평균가)\n\n" +
                            "**📊 분석 및 코멘트**\n" +
                            "- **상품 요약:** (상품 특징 1줄 요약)\n" +
                            "- **가격 분석:** (판매가가 시세 대비 어떤지, 구매/판매 추천 여부를 2~3문장으로 핵심만 요약)";
                    
                    history.add(Content.builder().role("user").parts(Collections.singletonList(Part.builder().text(finalPrompt).build())).build());

                    GenerateContentConfig searchConfig = GenerateContentConfig.builder()
                            .tools(Collections.singletonList(googleSearchTool)) // 구글 검색 도구만 활성화
                            .temperature(0.5f)
                            .maxOutputTokens(2500)
                            .build();

                    GenerateContentResponse finalResponse = geminiClient.models.generateContent("models/gemini-2.5-flash", history, searchConfig);

                    if (finalResponse != null && finalResponse.candidates().isPresent() && !finalResponse.candidates().get().isEmpty()) {
                        String finalText = finalResponse.text();
                        System.out.println("=== [Analyze Tool] Final Answer: " + finalText);
                        return finalText != null ? finalText : "분석 결과를 생성하지 못했습니다.";
                    }
                } else {
                    String text = response.text();
                    System.out.println("=== [Analyze Tool] Failed to call DB function: " + text);
                    return "AI가 상품 정보를 조회하지 못했습니다.";
                }
            }

        } catch (Exception e) {
            System.err.println("Gemini Analysis 오류: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 시세 분석 중 오류가 발생했습니다: " + e.getMessage();
        }

        return "시세 정보를 가져오지 못했습니다.";
    }

    /**
     * 시세 분석용 상품 상세 정보 조회 (Tool Execution)
     */
    private String executeProductDetailSearch(String productIdStr) {
        try {
            int productId = Integer.parseInt(productIdStr);
            com.numlock.pika.domain.Products product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return "Error: 해당 ID(" + productId + ")의 상품을 찾을 수 없습니다.";
            }

            // 검색 키워드 추출 (기존 extractSearchKeyword 로직 대신 제목을 그대로 사용하거나 간단히 처리)
            // 여기서는 제목을 그대로 제공하고 모델이 판단하게 함
            String keyword = product.getTitle();

            // 내부 평균 시세 조회
            Double internalAvg = productRepository.findAveragePriceByTitleAndCategory(
                    keyword,
                    product.getCategory().getCategoryId()
            );
            String avgPriceStr = (internalAvg != null) ? String.format("%,.0f원", internalAvg) : "데이터 부족으로 산출 불가";

            // JSON 형태 또는 명확한 텍스트로 반환
            return String.format(
                    "{" +
                    "\"productId\": %d, " +
                    "\"title\": \"%s\", " +
                    "\"price\": %s, " +
                    "\"category\": \"%s\", " +
                    "\"internalAveragePrice\": \"%s\", " +
                    "\"description\": \"%s\"" +
                    "}",
                    product.getProductId(),
                    product.getTitle(),
                    product.getPrice(),
                    product.getCategory().getCategory(),
                    avgPriceStr,
                    product.getDescription().replaceAll("[\"\\n]", " ") // 간단한 이스케이프 처리
            );

        } catch (NumberFormatException e) {
            return "Error: 유효하지 않은 상품 ID 형식입니다.";
        } catch (Exception e) {
            return "Error: DB 조회 중 오류 발생 - " + e.getMessage();
        }
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

            // 1. 도구(Function) 정의
            Tool searchTool = Tool.builder()
                    .functionDeclarations(Collections.singletonList(
                            FunctionDeclaration.builder()
                                    .name("search_market_products")
                                    .description("사용자가 특정 상품의 시세, 재고, 구매 가능 여부, 상품 목록 등을 물어볼 때 DB에서 상품을 검색합니다. 사용자가 구체적인 상품명을 언급하지 않아도 문맥상 상품 검색이 필요하면 사용하세요.")
                                    .parameters(
                                            Schema.builder()
                                                    .type(Type.Known.OBJECT)
                                                    .properties(Map.of(
                                                            "keyword", Schema.builder()
                                                                    .type(Type.Known.STRING)
                                                                    .description("검색할 상품명 (예: 원피스, 아이폰, 자전거)")
                                                                    .build()
                                                    ))
                                                    .required(Collections.singletonList("keyword"))
                                                    .build()
                                    )
                                    .build()
                    ))
                    .build();

            // 2. 시스템 프롬프트 설정 (히스토리가 비었을 때만)
            if (history.isEmpty()) {
                String systemInstruction = "당신은 중고거래 마켓 'Pika'의 AI 어시스턴트입니다.\n" +
                        "- 사용자가 상품 정보를 물으면 `search_market_products` 도구를 사용하여 실제 DB 데이터를 확인한 후 답변하세요.\n" +
                        "- 도구 실행 결과가 없으면 '현재 판매 중인 해당 상품이 없습니다'라고 정직하게 말하세요.\n" +
                        "- 답변은 친절하고 간결하게(3~4문장) 작성하세요.";
                
                Content systemContent = Content.builder()
                        .role("user")
                        .parts(Collections.singletonList(Part.builder().text(systemInstruction).build()))
                        .build();
                history.add(systemContent);
                
                // 턴을 맞추기 위한 모델의 더미 응답
                history.add(Content.builder().role("model").parts(Collections.singletonList(Part.builder().text("네, 알겠습니다.").build())).build());
            }

            // 3. 사용자 메시지 추가
            Content userContent = Content.builder()
                    .role("user")
                    .parts(Collections.singletonList(Part.builder().text(userMessage).build()))
                    .build();
            history.add(userContent);

            // 4. 1차 호출 (Tools 포함)
            GenerateContentConfig chatConfig = GenerateContentConfig.builder()
                    .tools(Collections.singletonList(searchTool))
                    .maxOutputTokens(3000)
                    .temperature(0.7f)
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", history, chatConfig);

            // 5. 응답 처리 (함수 호출 vs 텍스트 답변)
            if (response != null && response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                com.google.genai.types.Candidate candidate = response.candidates().get().get(0);
                
                // 모델의 1차 응답(함수 호출 요청 포함 가능)을 히스토리에 저장
                if (candidate.content().isPresent()) {
                    history.add(candidate.content().get());
                }

                List<Part> parts = Collections.emptyList();
                if (candidate.content().isPresent() && candidate.content().get().parts().isPresent()) {
                    parts = candidate.content().get().parts().get();
                }

                boolean functionCalled = false;
                List<Part> functionResponseParts = new ArrayList<>();

                for (Part part : parts) {
                    // FunctionCall 확인 (Optional 처리)
                    if (part.functionCall().isPresent()) {
                        FunctionCall call = part.functionCall().get();
                        
                        if (call.name().isPresent() && "search_market_products".equals(call.name().get())) {
                            functionCalled = true;
                            Map<String, Object> args = call.args().orElse(Collections.emptyMap());
                            String keyword = (String) args.get("keyword");
                            
                            System.out.println("=== [Tool Use] Gemini requests function: search_market_products(" + keyword + ") ===");

                            // DB 조회 실행
                            String searchResult = executeProductSearch(keyword);

                            // 결과 생성 (FunctionResponse)
                            functionResponseParts.add(Part.builder()
                                    .functionResponse(FunctionResponse.builder()
                                            .name(call.name().get())
                                            .response(Map.of("result", searchResult))
                                            .build())
                                    .build());
                        }
                    }
                }

                if (functionCalled) {
                    // 6. 함수 실행 결과를 모델에게 전달 (2차 호출)
                    Content functionResponseContent = Content.builder()
                            .role("function") // 중요: 역할은 function
                            .parts(functionResponseParts)
                            .build();
                    history.add(functionResponseContent);

                    // 도구 결과를 포함하여 다시 모델 호출 (최종 답변 생성)
                    GenerateContentResponse finalResponse = geminiClient.models.generateContent("models/gemini-2.5-flash", history, chatConfig);
                    
                    if (finalResponse != null && finalResponse.candidates().isPresent() && !finalResponse.candidates().get().isEmpty()) {
                         String finalText = finalResponse.text();
                         System.out.println("=== [Tool Use] Final Answer: " + finalText);
                         
                         // 최종 답변 히스토리 저장
                         if (finalResponse.candidates().get().get(0).content().isPresent()) {
                             history.add(finalResponse.candidates().get().get(0).content().get());
                         }
                         return finalText;
                    }
                } else {
                    // 함수 호출 없이 바로 답변이 온 경우
                    String text = response.text();
                    System.out.println("=== [Chat] Normal Response: " + text);
                    return text;
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini Chat 오류: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 서비스 연결 중 오류가 발생했습니다.";
        }
        return "죄송합니다. 이해하지 못했습니다.";
    }

    private String executeProductSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "검색어가 유효하지 않습니다.";
        }

        try {
            // DB에서 상품 검색 (최신순 5개)
            org.springframework.data.domain.Page<com.numlock.pika.domain.Products> products =
                    productRepository.searchByFilters(keyword, null, org.springframework.data.domain.PageRequest.of(0, 5));

            if (products.hasContent()) {
                long totalElements = products.getTotalElements();
                StringBuilder sb = new StringBuilder();
                sb.append("DB 검색 결과 ('").append(keyword).append("'): 총 ").append(totalElements).append("건 발견.\n")
                        .append("최신 등록 상품 5건:\n");

                for (com.numlock.pika.domain.Products p : products.getContent()) {
                    sb.append("- [").append(p.getProductState() == 0 ? "판매중" : "판매완료").append("] ")
                            .append(p.getTitle()).append(" / 가격: ").append(p.getPrice()).append("원\n");
                }
                return sb.toString();
            } else {
                return "검색 결과: '" + keyword + "' 관련 상품이 마켓에 없습니다.";
            }
        } catch (Exception e) {
            return "상품 검색 중 시스템 오류가 발생했습니다: " + e.getMessage();
        }
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
                String resultText = response.text();
                return (resultText != null) ? resultText.trim() : "리뷰 내용을 요약할 수 없습니다.";
            }
        } catch (Exception e) {
            // 7. 에러 발생 시 원인을 구체적으로 로그에 남김
            System.err.println("Gemini 요약 API 오류: " + e.getMessage());
            return "리뷰를 분석 중입니다. 잠시 후 확인해 주세요.";
        }

        return "리뷰 요약을 생성할 수 없습니다.";
    }
}
