document.querySelectorAll('.thumbnail').forEach(img => {
    img.addEventListener('click', () => {
        document.querySelector('.main-image img').src = img.src;

        document.querySelectorAll('.thumbnail')
            .forEach(t => t.classList.remove('active'));

        img.classList.add('active');
    });
});

async function onclickConfirmPayment() {
    const impUid = document.getElementById('impUid').value;
    if (!impUid) {
        alert("결제 정보를 찾을 수 없습니다.");
        return;
    }
    try {
        const serverResponse = await fetch(`/api/payment/confirm/${impUid}`, {method: "POST"});
        const data = await serverResponse.json();
        if (!serverResponse.ok) {
            throw new Error(data.message || '서버 검증 응답 오류');
        }
        alert("구매 확정 성공!");
        // Redirect to the review page after successful payment confirmation
        const productId = document.querySelector('.product-id').value;
        const sellerId = document.querySelector('.seller-id').value;
        const sellerNickname = document.querySelector('.seller-info p:first-of-type').textContent; // Assuming the first <p> in seller-info is the nickname
        window.location.href = `/reviews/new?productId=${productId}&sellerId=${sellerId}&sellerNickname=${encodeURIComponent(sellerNickname)}`;
    } catch (e) {
        console.error("서버 검증 실패:", e);
        alert('구매 확정 실패: ' + e.message);
    }
}

async function onclickCancelPayment() {
    const impUid = document.getElementById('impUid').value;
    if (!impUid) {
        alert("결제 정보를 찾을 수 없습니다.");
        return;
    }

    try {
        const resp = await fetch(`/api/payments/cancel`, {
            method: "DELETE", // 컨트롤러 @DeleteMapping과 일치시킴
            headers: {
                "Content-Type": "application/json" // JSON 전송 명시
            },
            body: JSON.stringify({impUid: impUid}) // 데이터를 JSON 문자열로 변환
        });

        if (resp.ok) {
            const result = await resp.text(); // 서버에서 리턴한 숫자 '3'을 가져옴
            console.log("결과 코드:", result);
            alert("결제 취소/환불 완료");
            window.location.reload();
        } else {
            console.error('서버 오류: ' + resp.status);
            alert("취소 처리 중 오류가 발생했습니다.");
        }
    } catch (error) {
        console.error('네트워크 에러:', error);
        alert('구매 취소/환불 실패');
    }
}

async function onclickApprovePayment() {

    //계좌가 존재하는지 확인하는 api
    const checkResp = await fetch(`/api/accounts/exists`);
    const hasAccount = await checkResp.json(); // 서버에서 boolean(true/false) 반환

    if (!hasAccount) {
        // 계좌가 없으면(false) 알림 후 마이페이지로 이동
        alert("계좌 등록을 해야 합니다. \n마이페이지 - 계좌 관리");
        window.location.href = "/user/mypage";
        return;
    }

    const productId = document.querySelector('.product-id').value;

    try {
        const resp = await fetch(`/api/payments/approve`, {
            method: "PUT", // 컨트롤러 @PutMapping과 일치시킴
            headers: {
                "Content-Type": "application/json" // JSON 전송 명시
            },
            body: JSON.stringify({taskId: productId}) // 데이터를 JSON 문자열로 변환
        });

        if (resp.ok) {
            const result = await resp.text(); // 서버에서 리턴한 숫자 '3'을 가져옴
            console.log("결과 코드:", result);
            alert("결제 승인 완료");
            window.location.reload();
        } else {
            console.error('서버 오류: ' + resp.status);
            alert("승인 처리 중 오류가 발생했습니다.");
        }
    } catch (error) {
        console.error('네트워크 에러:', error);
        alert('결제 승인 실패');
    }
}

const wishBtn = document.querySelector(".wish-btn");

if (wishBtn) {
    wishBtn.addEventListener('click', () => {
        const productId = document.querySelector('.product-id').value;
        const wishedInput = document.querySelector('.wished');

        const isWished = wishedInput.value === 'true';
        const httpMethod = isWished ? "DELETE" : "POST";

        fetch(`/api/product/${productId}/wish`, {method: httpMethod})
            .then(resp => {
                if (!resp.ok) throw new Error(`요청 실패: ${resp.status}`);
                return resp.json();
            })
            .then(data => {
                if (httpMethod === 'POST') {
                    addFavoriteItem(data);
                } else {
                    removeFavoriteItem(productId);
                }

                const newWishCnt = parseInt(data.fpCnt);
                const wishCntElement = document.querySelector('.wish-cnt span');
                if (wishCntElement) {
                    wishCntElement.textContent = newWishCnt;
                }

                const newIsWished = !isWished;
                if (newIsWished) {
                    wishBtn.classList.add('wished-active');
                } else {
                    wishBtn.classList.remove('wished-active');
                }
                wishedInput.value = newIsWished.toString();
            })
            .catch(error => {
                console.error('찜 처리 중 오류 발생:', error);
                alert(`찜 처리 중 오류가 발생했습니다. (${error.message})`);
            });
    });
}

// 판매자 리뷰 요약 가져오기
document.addEventListener('DOMContentLoaded', function () {
    const showReviewSummaryBtn = document.getElementById('showReviewSummaryBtn');
    const reviewSummaryContent = document.getElementById('reviewSummaryContent');
    const productIdInput = document.querySelector('.product-id');
    const sellerIdInput = document.querySelector('.seller-id');
    const productStatInput = document.getElementById('productStat'); // 새로 추가된 input

    if (showReviewSummaryBtn && reviewSummaryContent && productIdInput && sellerIdInput && productStatInput) {
        // 버튼이 항상 클릭 가능하도록 유지
        showReviewSummaryBtn.style.pointerEvents = 'auto';

        showReviewSummaryBtn.addEventListener('click', async () => {
            console.log("리뷰 보기 버튼 클릭됨 (info.js)");
            reviewSummaryContent.textContent = '요약 로딩 중...';

            const productId = productIdInput.value;
            const sellerId = sellerIdInput.value;
            const productStat = productStatInput.value; // productStat 값 가져오기

            try {
                // productId와 productStat을 쿼리 파라미터로 함께 전달
                const response = await fetch(`/reviews/summary/${sellerId}?productId=${productId}&productStat=${productStat}`);
                if (!response.ok) {
                    throw new Error('리뷰 요약을 가져오는 데 실패했습니다.');
                }
                const summary = await response.text();
                reviewSummaryContent.textContent = summary;
                // 로딩 후 버튼 숨김 처리 제거
                // showReviewSummaryBtn.style.display = 'none';
            } catch (error) {
                console.error('Error fetching review summary:', error);
                reviewSummaryContent.textContent = '리뷰 요약 로딩 실패';
                reviewSummaryContent.style.color = 'red';
            }
        });
    }
});

async function analyzePrice() {
    const productId = document.querySelector('.product-id').value;

    // 1. 채팅창 열기 (header.html에 있는 요소 및 함수 활용)
    const chatWindow = document.getElementById('ai-chat-window');
    if (chatWindow) {
        chatWindow.style.display = 'flex';
    }

    // 2. 로딩 메시지 표시 (header.html에 정의된 함수 사용)
    if (typeof appendMessage === 'function') {
        const loadingId = 'loading-analyze-' + Date.now();
        appendMessage('🔍 현재 상품의 시세를 분석 중입니다... 잠시만 기다려주세요.', 'ai', loadingId);

        try {
            // 3. API 호출
            const response = await fetch(`/api/chat/analyze/${productId}`, {
                method: 'POST'
            });

            if (!response.ok) throw new Error('Network response was not ok');

            const data = await response.json();

            // 4. 로딩 제거 및 결과 표시
            const loadingElement = document.getElementById(loadingId);
            if (loadingElement) loadingElement.remove();

            appendMessage(data.response, 'ai');

        } catch (error) {
            console.error('Analysis Error:', error);
            const loadingElement = document.getElementById(loadingId);
            if (loadingElement) loadingElement.remove();
            appendMessage('시세 분석 중 오류가 발생했습니다. 관리자에게 문의해주세요.', 'ai');
        }
    } else {
        console.error("appendMessage function not found");
        alert("AI 챗봇 기능이 아직 로드되지 않았습니다. 새로고침 후 다시 시도해주세요.");
    }
}