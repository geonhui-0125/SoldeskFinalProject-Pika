# PIKA

서브컬처 굿즈의 탐색과 거래를 위한 웹 기반 중고거래 플랫폼입니다. 판매자는 상품을 등록하고 AI 시세 정보를 참고할 수 있으며, 구매자는 검색·카테고리·찜 기능으로 상품을 찾고 판매자와 채팅할 수 있습니다. 프로젝트 기획 배경과 팀별 구현 범위는 상위 저장소의 [`docs/`](../../docs/) 자료를 참고하세요.

## 주요 기능

- 회원 가입, 로그인, 계정 관리 및 비밀번호 재설정 이메일
- 상품 등록·수정·조회, 키워드/카테고리 검색, 인기 검색어
- 찜 목록, 마이페이지, 구매·판매 내역 및 알림
- WebSocket 기반 사용자 간 실시간 채팅과 이미지 첨부
- PortOne 결제 정보 확인 및 서버 측 결제 검증
- Gemini API를 이용한 상품 시세 분석과 리뷰 요약
- Spring Security 기반 접근 제어와 JWT 유틸리티

## 기술 스택

- Java 17, Spring Boot 3.4.12, Gradle
- Spring MVC, Thymeleaf, Spring Security, Spring Data JPA, WebSocket
- Oracle Database (ojdbc11)
- Google Gemini, PortOne(Iamport), CoolSMS, SMTP 연동 라이브러리

## 준비 사항

- JDK 17
- Oracle Database 접속 정보 및 프로젝트 스키마
- 외부 서비스 기능을 사용할 경우 해당 서비스의 API 자격 증명

Gradle Wrapper가 포함되어 있으므로 별도의 Gradle 설치 없이 실행할 수 있습니다. 최초 실행 시 Gradle 의존성을 내려받을 수 있도록 네트워크 연결이 필요합니다.

## 설정

애플리케이션은 `src/main/resources/application.properties`의 설정을 읽습니다. 저장소에 실제 자격 증명을 넣지 말고, 아래 항목을 로컬 설정 파일 또는 환경 변수로 제공하세요. Spring Boot는 `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`처럼 속성명을 대문자 스네이크 케이스 환경 변수로 바인딩합니다.

필수 설정:

| 속성 | 설명 |
| --- | --- |
| `spring.datasource.url` | Oracle JDBC URL (현재 기본 예시: `jdbc:oracle:thin:@localhost:1521:XE`) |
| `spring.datasource.username` | Oracle 사용자 |
| `spring.datasource.password` | Oracle 비밀번호 |
| `imp.code` | PortOne 가맹점 식별 코드 |
| `imp.access` | PortOne API 키 |
| `imp.secret` | PortOne API 시크릿 |
| `spring.mail.username`, `spring.mail.password`, `spring.mail.properties.mail.smtp.from` | 비밀번호 재설정 메일 발송 계정 정보 |
| `coolsms.apiKey`, `coolsms.secretkey` | CoolSMS 자격 증명 |
| `file.upload-path`, `file.chat-upload-path` | 상품 및 채팅 첨부 파일 저장 디렉터리 |
| `gemini.api.key` | Gemini API 키 |
| `jwt.secret` | 256비트 이상 난수 서명 키 |

`application.properties`에 작성하는 경우 `${ENV_VAR}` 형식으로 외부 환경 변수를 참조할 수 있습니다. 예: `spring.datasource.password=${DB_PASSWORD}`. 파일 저장 경로 디렉터리를 미리 만들고 애플리케이션 프로세스가 쓸 수 있게 권한을 부여하세요. 프로필 이미지 업로드는 코드상 사용자 홈의 `pika_uploads/profile` 디렉터리를 사용합니다.

데이터베이스 테이블 생성 스크립트가 필요하면 저장소 루트의 [`table_create_query.txt`](../table_create_query.txt)를 확인하세요. 실행 전에 연결 대상 Oracle 버전과 스키마에 맞는지 검토하고, 앱 계정이 해당 스키마에 접근 가능한지 확인하세요.

## 실행

프로젝트 디렉터리(`SoldeskFinalProject-Pika/pika`)에서 실행합니다.

```bash
# macOS / Linux
./gradlew bootRun

# Windows PowerShell 또는 명령 프롬프트
gradlew.bat bootRun
```

애플리케이션은 별도 포트 설정이 없으면 Spring Boot 기본 포트 `8080`에서 실행됩니다. 브라우저에서 <http://localhost:8080/>을 여세요. 배포용 WAR는 다음 명령으로 만들 수 있으며 결과물은 `build/libs/`에 생성됩니다.

```bash
./gradlew clean bootWar
```

## 초기 관리자 계정

현재 `AdminUserInitializer`는 DB에 `admin` 사용자가 없으면 시작 시 관리자 계정을 생성합니다. 초기 비밀번호는 코드에 `ad123`으로 지정되어 있으므로 로컬 확인 후 즉시 변경하고, 실제 서비스에 배포하기 전에는 초기화 코드를 안전한 방식으로 바꾸세요.

## 유의 사항

- 외부 서비스 키, DB 비밀번호, JWT 시크릿은 커밋하지 마세요. 노출된 키는 폐기하고 재발급하세요.
- README 및 기본 설정만으로 데이터베이스 스키마나 외부 API 계정이 자동 준비되지는 않습니다.
- Gemini, PortOne, 메일, CoolSMS 기능은 각각 유효한 자격 증명과 서비스 측 설정이 있어야 동작합니다.
- 현재 저장소에는 `src/test` 테스트가 확인되지 않았습니다. 테스트가 추가되면 `./gradlew test`로 실행할 수 있습니다.

## 프로젝트 구조

```text
src/main/java/com/numlock/pika/
├── config/       # Spring, 보안, JWT, 정적 리소스 설정
├── controller/   # 웹 및 API 컨트롤러
├── domain/       # JPA 엔티티
├── dto/          # 요청·응답 객체
├── repository/   # 데이터 접근 계층
├── service/      # 비즈니스 로직
└── handler/      # WebSocket 메시지 처리
src/main/resources/
├── static/       # CSS, JavaScript, 아이콘, 기본 이미지
└── templates/    # Thymeleaf 화면
```
