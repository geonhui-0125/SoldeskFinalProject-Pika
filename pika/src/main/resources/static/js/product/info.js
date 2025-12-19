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
        window.location.reload();
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
            body: JSON.stringify({ impUid: impUid }) // 데이터를 JSON 문자열로 변환
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

document.querySelector(".wish-btn").addEventListener('click', () => {

    const likeButton = document.querySelector(".wish-btn");
    const productId = document.querySelector('.product-id').value;
    const wishedInput = document.querySelector('.wished'); // <input> 태그

    // 현재 찜 상태 확인 (boolean 값으로 변환)
    const isWished = wishedInput.value === 'true';

    // 찜 상태에 따라 사용할 HTTP 메소드 결정
    // isWished가 true (찜한 상태)이면 -> DELETE (취소)
    // isWished가 false (찜 안 한 상태)이면 -> POST (찜하기)
    const httpMethod = isWished ? "DELETE" : "POST";

    fetch(`/api/product/${productId}/wish`,
        {method: httpMethod})
        .then(resp => {
            if (resp.ok) {
                return resp.text();
            }
            // 서버에서 오류가 발생하면 이 Error를 throw
            throw new Error(`요청 실패: ${resp.status}`);
        })
        .then(data => {
            const newWishCnt = parseInt(data);

            console.log('변화된 wishCnt : ', newWishCnt);

            // 찜 카운트 업데이트
            const wishCntElement = document.querySelector('.wish-cnt span');
            if (wishCntElement) {
                wishCntElement.textContent = newWishCnt;
            }

            // 상태 반전 (찜 취소했으면 false로, 찜 했으면 true로)
            const newIsWished = !isWished;

            // 버튼 클래스 토글 (버튼 효과 적용/제거)
            if (newIsWished) {
                likeButton.classList.add('wished-active'); // 찜하기 완료 -> 활성화 클래스 추가
            } else {
                likeButton.classList.remove('wished-active'); // 찜 취소 완료 -> 활성화 클래스 제거
            }

            // 숨겨진 wished 필드 값 업데이트
            wishedInput.value = newIsWished.toString();

        })
        .catch(error => {
            console.error('찜 처리 중 오류 발생:', error);
            alert(`찜 처리 중 오류가 발생했습니다. (${error.message})`);
        });

});

// 판매자 리뷰 요약 가져오기
document.addEventListener('DOMContentLoaded', function () {
    const sellerId = document.querySelector('.seller-id').value;
    const reviewSummaryElement = document.querySelector('.review-summary');

    if (sellerId && reviewSummaryElement) {
        fetch(`/reviews/summary/${sellerId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('리뷰 요약을 가져오는 데 실패했습니다.');
                }
                return response.text();
            })
            .then(summary => {
                reviewSummaryElement.textContent = summary;
            })
            .catch(error => {
                console.error('Error fetching review summary:', error);
                reviewSummaryElement.textContent = '리뷰 요약 로딩 실패';
                reviewSummaryElement.style.color = 'red';
            });
    }
});