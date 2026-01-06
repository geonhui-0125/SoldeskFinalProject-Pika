package com.numlock.pika.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Tool;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
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
    
    // 최대 도구 사용 반복 횟수
    private static final int MAX_TOOL_TURNS = 5;

    public GeminiService(com.numlock.pika.repository.ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 시세 분석 결과를 채팅 히스토리에 수동으로 삽입                                   
     * 분석 후 일반 채팅으로 대화를 이어갈 때 AI가 분석 내용을 기억
     */
    public void saveAnalysisToHistory(String sessionId, String productName, String analysisResult) {
        List<Content> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());
                        
        // 시스템 안내가 아직 없다면 추가 (채팅 기능 활성화)
        if (history.isEmpty()) {
            addSystemInstruction(history);    
        }

        // 사용자가 분석을 요청한 것처럼 기록
        history.add(Content.builder().role("user")
                .parts(Collections.singletonList(Part.builder().text(productName + " 상품의 시세 분석을 보여줘.").build())).build());

        // AI가 분석 결과를 답변한 것처럼 기록
        history.add(Content.builder().role("model")
                .parts(Collections.singletonList(Part.builder().text(analysisResult).build())).build());
        
        System.out.println("=== [Memory] Analysis result saved to session: " + sessionId + " ===");
    }

    private void addSystemInstruction(List<Content> history) {
        String systemInstruction = "당신은 중고마켓 Pika의 AI입니다. 상품 검색 결과가 '0건'이면, 즉시 다른 줄임말(예: 귀칼, 롤)이나 핵심 키워드로 다시 검색 도구를 호출하세요. 최대 3~4회까지 시도해도 없으면 없다고 답하세요.";
        history.add(Content.builder().role("user").parts(Collections.singletonList(Part.builder().text(systemInstruction).build())).build());
        history.add(Content.builder().role("model").parts(Collections.singletonList(Part.builder().text("네, 알겠습니다.").build())).build());
    }

    @PostConstruct
    public void init() {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini API Key가 설정되지 않았습니다.");
        }
        this.geminiClient = Client.builder()
                .apiKey(geminiApiKey)
                .httpOptions(HttpOptions.builder().apiVersion("v1beta").build())
                .build();
    }

    /**
     * 상품 시세 분석 (함수 호출과 구글 검색)
     * 상품 ID 기반 조회이므로 단일 프로세스로 진행
     */
    public String analyzeProductPrice(int productId) {
        List<Content> history = new ArrayList<>();

        Tool dbTool = Tool.builder()
                .functionDeclarations(Collections.singletonList(
                        FunctionDeclaration.builder()
                                .name("get_product_detail")
                                .description("상품 ID를 입력받아 상품의 상세 정보와 내부 평균 거래가를 조회합니다.")
                                .parameters(
                                        Schema.builder()
                                                .type(Type.Known.OBJECT)
                                                .properties(Map.of(
                                                        "productId", Schema.builder().type(Type.Known.STRING).description("상품 ID").build()
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

        String prompt = String.format("상품 ID '%d'번에 대한 시세 분석을 시작해. 먼저 `get_product_detail` 도구로 정보를 가져와줘.", productId);
        history.add(Content.builder().role("user").parts(Collections.singletonList(Part.builder().text(prompt).build())).build());

        try {
            //DB 조회
            GenerateContentConfig dbConfig = GenerateContentConfig.builder()
                    .tools(Collections.singletonList(dbTool))
                    .temperature(0.1f)
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", history, dbConfig);

            if (response != null && response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                com.google.genai.types.Candidate candidate = response.candidates().get().get(0);
                if (candidate.content().isPresent()) history.add(candidate.content().get());

                List<Part> parts = candidate.content().isPresent() && candidate.content().get().parts().isPresent() 
                        ? candidate.content().get().parts().get() : Collections.emptyList();

                boolean functionCalled = false;
                List<Part> functionResponseParts = new ArrayList<>();

                for (Part part : parts) {
                    if (part.functionCall().isPresent()) {
                        FunctionCall call = part.functionCall().get();
                        if ("get_product_detail".equals(call.name().orElse(""))) {
                            functionCalled = true;
                            String idStr = (String) call.args().orElse(Collections.emptyMap()).get("productId");
                            System.out.println("=== [Analyze] DB Search: " + idStr + " ===");
                            
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
                    history.add(Content.builder().role("function").parts(functionResponseParts).build());

                    //구글 검색 및 분석
                    String finalPrompt = "확인된 상품명으로 구글 검색을 수행하여 '정가'와 '중고 시세'를 찾아줘. 답변 시 Product ID나 상품 번호는 절대 언급하지 말고, 오직 상품명을 기준으로 3~5줄 이내로 핵심만 간결하게 분석해줘.";
                    history.add(Content.builder().role("user").parts(Collections.singletonList(Part.builder().text(finalPrompt).build())).build());

                    GenerateContentConfig searchConfig = GenerateContentConfig.builder()
                            .tools(Collections.singletonList(googleSearchTool))
                            .temperature(0.5f)
                            .maxOutputTokens(2500)
                            .build();

                    GenerateContentResponse finalResponse = geminiClient.models.generateContent("models/gemini-2.5-flash", history, searchConfig);
                    return finalResponse.text();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "분석 중 오류가 발생했습니다.";
        }
        return "정보를 가져오지 못했습니다.";
    }

    private String executeProductDetailSearch(String productIdStr) {
        try {
            int productId = Integer.parseInt(productIdStr);
            com.numlock.pika.domain.Products product = productRepository.findById(productId).orElse(null);
            if (product == null) return "Error: 상품 없음";

            Double internalAvg = productRepository.findAveragePriceByTitleAndCategory(product.getTitle(), product.getCategory().getCategoryId());
            String avgPriceStr = (internalAvg != null) ? String.format("%,.0f원", internalAvg) : "산출 불가";

            return String.format("{\"title\": \"%s\", \"price\": %s, \"avg\": \"%s\", \"desc\": \"%s\"}",
                    product.getTitle(), product.getPrice(), avgPriceStr, product.getDescription().replaceAll("[\"\\n]", " "));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * AI 채팅 (자동 재검색 루프 및 횟수 제한 적용)
     * 결과가 없으면 AI가 스스로 판단하여 줄임말/동의어로 재검색을 시도
     */
    public String getChatResponse(String sessionId, String userMessage) {
        try {
            List<Content> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());

            Tool searchTool = Tool.builder()
                    .functionDeclarations(List.of(
                            FunctionDeclaration.builder()
                                    .name("search_market_products")
                                    .description("우리 쇼핑몰(Pika) 내부의 상품 재고, 가격을 DB에서 검색합니다.")
                                    .parameters(
                                            Schema.builder()
                                                    .type(Type.Known.OBJECT)
                                                    .properties(Map.of("keyword", Schema.builder().type(Type.Known.STRING).description("상품명").build()))
                                                    .required(Collections.singletonList("keyword"))
                                                    .build()
                                    )
                                    .build(),
                            FunctionDeclaration.builder()
                                    .name("search_web_info")
                                    .description("쇼핑몰 내부에 없는 일반적인 정보, 외부 중고 시세, 뉴스 등을 구글에서 검색합니다.")
                                    .parameters(
                                            Schema.builder()
                                                    .type(Type.Known.OBJECT)
                                                    .properties(Map.of("query", Schema.builder().type(Type.Known.STRING).description("검색어").build()))
                                                    .required(Collections.singletonList("query"))
                                                    .build()
                                    )
                                    .build()
                    ))
                    .build();

            // 히스토리가 비었을 때만 시스템 지침 추가
            if (history.isEmpty()) {
                addSystemInstruction(history);
            }

            history.add(Content.builder().role("user").parts(Collections.singletonList(Part.builder().text(userMessage).build())).build());

            GenerateContentConfig chatConfig = GenerateContentConfig.builder()
                    .tools(Collections.singletonList(searchTool)) // Function Call만 설정 (Web Search는 함수 내부에서 우회 호출)
                    .maxOutputTokens(3000)
                    .temperature(0.7f)
                    .build();

            int turnCount = 0;

            // 루프 시작. 모델이 만족하거나 한계에 도달할 때까지 반복
            while (turnCount < MAX_TOOL_TURNS) {
                GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", history, chatConfig);
                
                if (response == null || response.candidates().isEmpty() || response.candidates().get().isEmpty()) {
                    return "응답을 생성할 수 없습니다.";
                }

                com.google.genai.types.Candidate candidate = response.candidates().get().get(0);
                if (candidate.content().isPresent()) {
                    history.add(candidate.content().get()); // 모델의 생각이나 도구 호출을 기록
                }

                List<Part> parts = candidate.content().isPresent() && candidate.content().get().parts().isPresent()
                        ? candidate.content().get().parts().get() : Collections.emptyList();

                boolean functionCalled = false;
                List<Part> functionResponseParts = new ArrayList<>();

                for (Part part : parts) {
                    if (part.functionCall().isPresent()) {
                        FunctionCall call = part.functionCall().get();
                        String callName = call.name().orElse("");

                        if ("search_market_products".equals(callName)) {
                            functionCalled = true;
                            String keyword = (String) call.args().orElse(Collections.emptyMap()).get("keyword");
                            System.out.println("=== [Chat Loop " + (turnCount + 1) + "] DB Searching: " + keyword + " ===");

                            String result = executeProductSearch(keyword);
                            
                            // 힌트 추가: 결과가 없으면 재검색 유도
                            if (result.contains("발견되지 않았습니다")) {
                                result += " (힌트: 다른 키워드나 줄임말로 다시 시도해보세요.)";
                            }

                            functionResponseParts.add(Part.builder()
                                    .functionResponse(FunctionResponse.builder()
                                            .name(callName)
                                            .response(Map.of("result", result))
                                            .build())
                                    .build());
                        } else if ("search_web_info".equals(callName)) {
                            functionCalled = true;
                            String query = (String) call.args().orElse(Collections.emptyMap()).get("query");
                            System.out.println("=== [Chat Loop " + (turnCount + 1) + "] Web Searching (Proxy): " + query + " ===");

                            String webResult = executeGoogleSearch(query);

                            functionResponseParts.add(Part.builder()
                                    .functionResponse(FunctionResponse.builder()
                                            .name(callName)
                                            .response(Map.of("result", webResult))
                                            .build())
                                    .build());
                        }
                    }
                }

                if (functionCalled) {
                    // 도구 실행 결과를 모델에게 전달하고 루프 계속 (모델이 결과를 보고 판단)
                    history.add(Content.builder().role("function").parts(functionResponseParts).build());
                    turnCount++;
                } else {
                    // 도구 호출이 없으면 최종 텍스트 답변이므로 루프 종료 및 반환
                    return response.text();
                }
            }

            return "여러 번 검색해 보았지만, 해당 상품 정보를 찾을 수 없습니다.";

        } catch (Exception e) {
            e.printStackTrace();
            return "서비스 연결 중 오류가 발생했습니다.";
        }
    }

    private String executeProductSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return "검색어 오류";
        try {
            org.springframework.data.domain.Page<com.numlock.pika.domain.Products> products =
                    productRepository.searchByFilters(keyword, null, org.springframework.data.domain.PageRequest.of(0, 5));

            if (products.hasContent()) {
                StringBuilder sb = new StringBuilder();
                sb.append("검색 결과 '" + keyword + "' 총 ").append(products.getTotalElements()).append("건:\n");
                for (com.numlock.pika.domain.Products p : products.getContent()) {
                    sb.append("- [" + (p.getProductState() == 0 ? "판매중" : "판매완료") + "] ")
                            .append(p.getTitle()).append(" / ").append(p.getPrice()).append("원\n");
                }
                return sb.toString();
            } else {
                return "키워드 '" + keyword + "' 상품이 발견되지 않았습니다.";
            }
        } catch (Exception e) {
            return "시스템 오류: " + e.getMessage();
        }
    }

    // 리뷰 요약 기능
    public String generateReviewSummary(List<String> reviewContents) {
        if (reviewContents == null || reviewContents.isEmpty()) return "아직 등록된 리뷰가 없습니다.";
        List<String> limitedReviews = reviewContents.stream().limit(10).collect(Collectors.toList());
        String combinedReviews = String.join("\n", limitedReviews);
        String prompt = "다음 판매자 리뷰를 50자 이내 한줄평으로 요약해줘:\n" + combinedReviews;

        try {
            GenerateContentConfig summaryConfig = GenerateContentConfig.builder().maxOutputTokens(400).temperature(0.5f).build();
            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", prompt, summaryConfig);
            return (response != null) ? response.text().trim() : "요약 불가";
        } catch (Exception e) {
            return "리뷰 분석 중...";
        }
    }
    // Google Search 전용 독립 호출 메서드
    private String executeGoogleSearch(String query) {
        try {
            // Google Search 도구만 단독으로 설정
            Tool googleSearchTool = Tool.builder()
                    .googleSearch(GoogleSearch.builder().build())
                    .build();

            GenerateContentConfig searchConfig = GenerateContentConfig.builder()
                    .tools(Collections.singletonList(googleSearchTool))
                    .maxOutputTokens(1000)
                    .temperature(0.5f)
                    .build();

            // 문맥 없이 단발성 검색 수행
            String prompt = "다음 키워드에 대해 검색하고 핵심 정보를 요약해줘: " + query;
            GenerateContentResponse response = geminiClient.models.generateContent("models/gemini-2.5-flash", prompt, searchConfig);

            return response.text();
        } catch (Exception e) {
            return "웹 검색 중 오류 발생: " + e.getMessage();
        }
    }
}